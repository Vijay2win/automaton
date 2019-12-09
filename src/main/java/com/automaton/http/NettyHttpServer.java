package com.automaton.http;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.*;

public class NettyHttpServer {
    private static final Logger logger = LoggerFactory.getLogger(NettyHttpServer.class);

    private static final int MAX_POST = 1000000;

    public static final String HTTP_HANDLER_NAME = "http";
    private final ChannelGroup allChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;

    private final int port;
    private final int nThreads;
    private NettyHttpServer service = null;

    public NettyHttpServer(int port, int nThreads) {
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();
        this.port = port;
        this.nThreads = nThreads;
    }

    public CompletableFuture<Integer> create(HomekitConnectionFactory connectionFactory) {
        final CompletableFuture<Integer> portFuture = new CompletableFuture<>();
        ServerBootstrap b = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ServerInitializer(connectionFactory, allChannels, nThreads))
                .option(ChannelOption.SO_BACKLOG, Integer.valueOf(128))
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        final ChannelFuture bindFuture = b.bind(port);
        bindFuture.addListener(new GenericFutureListener<Future<? super Void>>() {
            public void operationComplete(Future<? super Void> future) throws Exception {
                try {
                    future.get();
                    SocketAddress socketAddress = bindFuture.channel().localAddress();
                    if (socketAddress instanceof InetSocketAddress) {
                        logger.info("Bound homekit listener to " + socketAddress.toString());
                        portFuture.complete(Integer.valueOf(((InetSocketAddress) socketAddress).getPort()));
                    } else {
                        throw new RuntimeException("Unknown socket address type: " + socketAddress.getClass().getName());
                    }
                } catch (Exception e) {
                    portFuture.completeExceptionally(e);
                }
            }
        });
        return portFuture;
    }

    class ServerInitializer extends ChannelInitializer<SocketChannel> {
        private final ChannelGroup allChannels;
        private final HomekitConnectionFactory homekit;
        private final EventExecutorGroup blockingExecutorGroup;

        public ServerInitializer(HomekitConnectionFactory homekit, ChannelGroup allChannels, int nThreads) {
            this.homekit = homekit;
            this.allChannels = allChannels;
            this.blockingExecutorGroup = (EventExecutorGroup) new DefaultEventExecutorGroup(nThreads);
        }

        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(new ChannelHandler[] { (ChannelHandler) new LoggingHandler() });
            pipeline.addLast("http", (ChannelHandler) new NettyHttpServer.AggregateResponseEncoder());
            pipeline.addLast(new ChannelHandler[] { (ChannelHandler) new HttpRequestDecoder() });
            pipeline.addLast(new ChannelHandler[] { (ChannelHandler) new HttpObjectAggregator(MAX_POST) });
            pipeline.addLast(blockingExecutorGroup,
                    new ChannelHandler[] { (ChannelHandler) new AccessoryHandler(homekit) });
            allChannels.add(ch);
        }
    }

    public CompletableFuture<Integer> start(HomekitConnectionFactory clientConnectionFactory) {
        if (service == null) {
            service = create(port, nThreads);
            return service.create(clientConnectionFactory);
        }
        throw new RuntimeException("HomekitHttpServer can only be started once");
    }

    public void shutdown() {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

    public void resetConnections() {
        logger.info("Resetting connections");
        allChannels.close();
    }

    public static NettyHttpServer create(int port, int nThreads) {
        return new NettyHttpServer(port, nThreads);
    }

    public static class AggregateResponseEncoder extends HttpResponseEncoder {
        protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
            super.encode(ctx, msg, out);
            if (out.size() > 0) {
                Iterator<Object> i = out.iterator();
                ByteBuf b = (ByteBuf) i.next();
                while (i.hasNext()) {
                    b.writeBytes((ByteBuf) i.next());
                    i.remove();
                }
            }
        }
    }
}
