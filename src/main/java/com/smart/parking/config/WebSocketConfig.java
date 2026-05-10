package com.smart.parking.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker   // turns on STOMP message broker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // Step A — register the connection endpoint.
    // Clients connect to ws://localhost:8080/ws
    // SockJS is a fallback library: if the browser doesn't support
    // native WebSocket, it falls back to HTTP long-polling automatically.
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
                .addEndpoint("/ws")          // the URL clients connect to
                .setAllowedOriginPatterns("*") // allow any origin (tighten in prod)
                .withSockJS();               // enable SockJS fallback
    }

    // Step B — configure the message broker.
    // /topic = broadcast channel (one server → many clients)
    // /app   = prefix for messages sent FROM client TO server
    //          (you don't use client→server messages in this sprint,
    //           but the prefix is required by STOMP)
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");      // server → clients
        registry.setApplicationDestinationPrefixes("/app"); // client → server
    }
}
