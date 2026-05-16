package com.freelance.freelance_platform.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final AppProperties appProperties;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Préfixe pour les messages envoyés au broker
        registry.enableSimpleBroker("/topic", "/queue");
        // Préfixe pour les messages destinés aux controllers
        registry.setApplicationDestinationPrefixes("/app");
        // Préfixe pour les messages privés (user-specific)
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(
                        appProperties.cors().allowedOrigins().toArray(String[]::new))
                .withSockJS();
    }
}