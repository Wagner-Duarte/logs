package com.example.demo.config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriTemplate;

import java.util.Map;

public class FileNameHandshakeInterceptor implements HandshakeInterceptor {

    private static final UriTemplate TEMPLATE = new UriTemplate("/logs-stream/{fileName}");

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String uri = request.getURI().getPath();
        Map<String, String> variables = TEMPLATE.match(uri);
        if (variables != null && variables.containsKey("fileName")) {
            attributes.put("fileName", variables.get("fileName"));
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
