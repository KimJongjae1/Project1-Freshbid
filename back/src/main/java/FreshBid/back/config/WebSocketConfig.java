package FreshBid.back.config;

import FreshBid.back.interceptor.JwtWebSocketHandshakeInterceptor;
import FreshBid.back.socket.BidHandler;
import FreshBid.back.socket.SignalingHandler;
import lombok.RequiredArgsConstructor;
import org.kurento.client.KurentoClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final JwtWebSocketHandshakeInterceptor jwtWebSocketHandshakeInterceptor;

    @Value("${socket.server.address}")
    private String address;

    @Value("${socket.server.port}")
    private String port;

    @Bean
    public SignalingHandler signalingHandler() {
        return new SignalingHandler();
    }

    @Value("${SOCKET_ADDRESS}")
    private String socketAddress;
    @Value("${SOCKET_PORT}")
    private String socketPort;
    
    @Bean
    public KurentoClient kurentoClient() {
        return KurentoClient.create(String.format("ws://%s:%s/kurento", socketAddress, socketPort));
    }

//    @Bean
//    public ServletServerContainerFactoryBean createServletServerContainerFactoryBean() {
//        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
//        container.setMaxTextMessageBufferSize(32768);
//        return container;
//    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(signalingHandler(), "/call")
            .addInterceptors(jwtWebSocketHandshakeInterceptor)
            .setAllowedOrigins("*");
    }
}
