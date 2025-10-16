package com.example.AssistantWebSocket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

public class CustomHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(ServerHttpRequest request,
                                      WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
        // Generate a unique Principal for each connection using a UUID.
        System.out.println( new StompPrincipal(UUID.randomUUID().toString()));
        return new StompPrincipal(UUID.randomUUID().toString());
    }
}