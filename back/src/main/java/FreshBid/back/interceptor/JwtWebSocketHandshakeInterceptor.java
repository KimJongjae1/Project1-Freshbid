package FreshBid.back.interceptor;

import FreshBid.back.dto.user.FreshBidUserDetails;
import FreshBid.back.service.FreshBidUserDetailsService;
import FreshBid.back.util.JwtTokenProvider;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtWebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final FreshBidUserDetailsService userDetailsService;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) throws Exception {
        
        String token = extractToken(request);
        
        if (token == null) {
            log.warn("JWT 토큰이 없습니다 - 연결을 거부합니다");
            return false;
        }

        try {
            // JWT 토큰 검증
            jwtTokenProvider.validateToken(token);

            // 사용자 정보 조회
            String username = jwtTokenProvider.extractUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (userDetails == null) {
                log.warn("사용자를 찾을 수 없습니다: {} - 연결을 거부합니다", username);
                return false;
            }

            // WebSocketSession attributes에 사용자 정보 저장
            attributes.put("userDetails", userDetails);
            log.info("WebSocket 인증 성공: {}", username);
            
            return true;
            
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰 - 연결을 거부합니다", e);
            return false;
        } catch (SecurityException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            log.warn("유효하지 않은 JWT 토큰 - 연결을 거부합니다", e);
            return false;
        } catch (Exception e) {
            log.error("JWT 토큰 검증 중 오류 발생 - 연결을 거부합니다", e);
            return false;
        }
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
        // 필요시 후처리 로직 추가
    }

    /**
     * Authorization 헤더 또는 쿼리 파라미터에서 JWT 토큰 추출
     */
    private String extractToken(ServerHttpRequest request) {
        // 1. Authorization 헤더에서 토큰 추출
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // 2. 쿼리 파라미터에서 토큰 추출 (fallback)
        String query = request.getURI().getQuery();
        if (query != null && query.contains("token=")) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("token=")) {
                    return param.substring(6);
                }
            }
        }

        return null;
    }
}