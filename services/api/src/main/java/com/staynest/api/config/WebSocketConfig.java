package com.staynest.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${app.frontend.customer-url:http://localhost:5173}")
    private String customerUrl;

    @Value("${app.frontend.admin-url:http://localhost:5174}")
    private String adminUrl;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // In-memory message broker for topics and queues
        registry.enableSimpleBroker("/topic", "/queue");
        // Prefix for messages from clients to application
        registry.setApplicationDestinationPrefixes("/app");
        // Prefix for user-specific destinations
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(customerUrl, adminUrl)
                .withSockJS();
    }
}
