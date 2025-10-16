package com.example.AssistantWebSocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

@Configuration
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages
                // Allow CONNECT, DISCONNECT, etc.
                .nullDestMatcher().permitAll()
                // Allow messages sent to your app destinations without requiring authentication
                .simpDestMatchers("/app/**").permitAll()
                // For user-specific messaging, allow subscription to /user/queue/** even if user is anonymous.
                .simpSubscribeDestMatchers("/user/**").permitAll()
                // Deny everything else
                .anyMessage().denyAll();
    }

    @Override
    protected boolean sameOriginDisabled() {
        // Disable same origin checks if needed.
        return true;
    }
}
