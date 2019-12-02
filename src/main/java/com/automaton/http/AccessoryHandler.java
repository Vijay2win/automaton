package com.automaton.http;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;

class AccessoryHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final static Logger LOGGER = LoggerFactory.getLogger(AccessoryHandler.class);

    private HomekitConnection connection;
    private final HomekitConnectionFactory homekitClientConnectionFactory;

    public AccessoryHandler(HomekitConnectionFactory homekitClientConnectionFactory) {
        this.homekitClientConnectionFactory = homekitClientConnectionFactory;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.pipeline().channel();
        this.connection = homekitClientConnectionFactory.createConnection(response -> {
            if (!channel.isActive())
                return;
            channel.writeAndFlush(HttpResponses.createResponse(response));
        });
        LOGGER.info("New homekit connection from {}", ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("Terminated homekit connection from {}", ctx.channel().remoteAddress());
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        try {
            HttpResponse response = connection.handleRequest(req);
            if (response.doUpgrade()) {
                ChannelPipeline pipeline = ctx.channel().pipeline();
                pipeline.addBefore(NettyHttpServer.HTTP_HANDLER_NAME, "binary", new BinaryHandler(connection));
            }
            sendResponse(response, ctx);
        } catch (Exception e) {
            LOGGER.error("Error handling homekit http request", e);
            sendResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, "Error: " + e.getMessage(), ctx);
        }
    }

    private void sendResponse(HttpResponseStatus status, String responseBody, ChannelHandlerContext ctx) {
        if (responseBody == null) {
            responseBody = "";
        }
        DefaultFullHttpResponse defaultFullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,
                Unpooled.copiedBuffer(responseBody.getBytes(StandardCharsets.UTF_8)));
        defaultFullHttpResponse.headers().set("Content-Type", "text/plain");
        defaultFullHttpResponse.headers().set("Content-Length",
                Integer.valueOf(defaultFullHttpResponse.content().readableBytes()));
        defaultFullHttpResponse.headers().set("Connection", "keep-alive");
        ctx.write(defaultFullHttpResponse);
        ctx.flush();
    }

    private void sendResponse(HttpResponse homekitResponse, ChannelHandlerContext ctx) {
        FullHttpResponse response = HttpResponses.createResponse(homekitResponse);
        ctx.write(response);
        ctx.flush();
    }

    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
        super.channelReadComplete(ctx);
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        boolean errorLevel = !(cause instanceof IOException);
        if (errorLevel) {
            LOGGER.error("Exception caught in web handler", cause);
        } else {
            LOGGER.debug("Exception caught in web handler", cause);
        }
        ctx.close();
    }
}
