package com.automaton.http;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableMap;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;

public class HttpResponses {
    private static final HttpVersion EVENT_VERSION = new HttpVersion("EVENT", 1, 0, true);

    public static class ConflictResponse implements HttpResponse {
        public int getStatusCode() {
            return 409;
        }
    }

    public static class InternalServerErrorResponse implements HttpResponse {
        private final Exception e;

        public InternalServerErrorResponse(Exception e) {
            this.e = e;
        }

        @Override
        public int getStatusCode() {
            return 500;
        }

        @Override
        public ByteBuffer getBody() {
            return ByteBuffer.wrap(e.getClass().getName().getBytes(StandardCharsets.UTF_8));
        }

        public Exception getException() {
            return e;
        }
    }

    public static class NotFoundResponse implements HttpResponse {
        public int getStatusCode() {
            return 404;
        }
    }

    public static class OkResponse implements HttpResponse {
        private final ByteBuffer body;

        public OkResponse(byte[] body) {
            this.body = ByteBuffer.wrap(body);
        }

        @Override
        public ByteBuffer getBody() {
            return body;
        }

        @Override
        public int getStatusCode() {
            return 200;
        }
    }

    public static class UnauthorizedResponse implements HttpResponse {
        @Override
        public int getStatusCode() {
            return 401;
        }
    }

    public static class HapJsonResponse extends OkResponse {
        private static final Map<String, String> HEADERS = ImmutableMap.of("Content-type", "application/hap+json");

        public HapJsonResponse(byte[] body) {
            super(body);
        }

        @Override
        public Map<String, String> getHeaders() {
            return HEADERS;
        }
    }

    public static class HapJsonNoContentResponse extends HapJsonResponse {
        public HapJsonNoContentResponse() {
            super(new byte[0]);
        }

        @Override
        public int getStatusCode() {
            return 204;
        }
    }

    public static class EventResponse extends HapJsonResponse {
        public EventResponse(byte[] body) {
            super(body);
        }

        public HttpVersion getVersion() {
            return HttpVersion.EVENT_1_0;
        }
    }

    public static class PairingResponse extends OkResponse {
        private static final Map<String, String> HEADERS = ImmutableMap.of("Content-type", "application/pairing+tlv8");

        public PairingResponse(byte[] body) {
            super(body);
        }

        @Override
        public Map<String, String> getHeaders() {
            return HEADERS;
        }
    }

    public static class UpgradeResponse extends PairingResponse {
        private final byte[] readKey;
        private final byte[] writeKey;

        public UpgradeResponse(byte[] body, byte[] readKey, byte[] writeKey) {
            super(body);
            this.readKey = readKey;
            this.writeKey = writeKey;
        }

        @Override
        public boolean doUpgrade() {
            return true;
        }

        public ByteBuffer getReadKey() {
            return ByteBuffer.wrap(readKey);
        }

        public ByteBuffer getWriteKey() {
            return ByteBuffer.wrap(writeKey);
        }
    }

    public static FullHttpResponse createResponse(HttpResponse homekitResponse) {
        DefaultFullHttpResponse defaultFullHttpResponse = new DefaultFullHttpResponse(
                (homekitResponse.getVersion() == HttpResponse.HttpVersion.EVENT_1_0) ? EVENT_VERSION
                        : HttpVersion.HTTP_1_1,
                HttpResponseStatus.valueOf(homekitResponse.getStatusCode()),
                Unpooled.copiedBuffer(homekitResponse.getBody()));
        for (Entry<String, String> header : homekitResponse.getHeaders().entrySet())
            defaultFullHttpResponse.headers().add(header.getKey(), header.getValue());
        defaultFullHttpResponse.headers().set("Content-Length",
                Integer.valueOf(defaultFullHttpResponse.content().readableBytes()));
        defaultFullHttpResponse.headers().set("Connection", "keep-alive");
        return defaultFullHttpResponse;
    }
}
