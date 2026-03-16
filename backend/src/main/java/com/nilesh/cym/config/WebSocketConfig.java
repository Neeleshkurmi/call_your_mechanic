package com.nilesh.cym.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final CorsConfigurationSource corsConfigurationSource;

    public WebSocketConfig(CorsConfigurationSource corsConfigurationSource) {
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        CorsConfiguration corsConfiguration = corsConfigurationSource.getCorsConfiguration(null);
        List<String> allowedOriginPatterns = corsConfiguration == null
                ? List.of("http://localhost:*", "http://127.0.0.1:*", "http://10.0.2.2:*")
                : corsConfiguration.getAllowedOriginPatterns();

        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(allowedOriginPatterns == null
                        ? new String[0]
                        : allowedOriginPatterns.toArray(new String[0]));
    }
}
