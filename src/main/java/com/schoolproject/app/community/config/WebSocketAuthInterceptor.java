package com.schoolproject.app.community.config;

import com.schoolproject.app.entity.User;
import com.schoolproject.app.repository.UserRepository;
import com.schoolproject.app.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("WebSocket CONNECT rejected: missing or invalid Authorization header");
                return message;
            }

            String token = authHeader.substring(7);
            if (!jwtTokenProvider.validateToken(token)) {
                log.warn("WebSocket CONNECT rejected: invalid JWT token");
                return message;
            }

            String username = jwtTokenProvider.getUsername(token);
            User user = userRepository.findByEmail(username).orElse(null);
            if (user == null) {
                log.warn("WebSocket CONNECT rejected: user not found for email {}", username);
                return message;
            }
            if (!user.isEnabled()) {
                log.warn("WebSocket CONNECT rejected: user {} is disabled", username);
                return message;
            }
            if (user.isLocked()) {
                log.warn("WebSocket CONNECT rejected: user {} is locked", username);
                return message;
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
            accessor.setUser(auth);
        }
        return message;
    }
}
