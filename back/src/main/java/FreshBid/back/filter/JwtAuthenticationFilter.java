package FreshBid.back.filter;

import FreshBid.back.dto.common.CommonResponse;
import FreshBid.back.repository.AuthTokenRedisRepository;
import FreshBid.back.service.FreshBidUserDetailsService;
import FreshBid.back.util.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    private final FreshBidUserDetailsService freshBidUserDetailsService;

    private static final String[] NO_AUTH_PATTERNS = {
        "/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**",
        "/index.html", "/call/**", "/ws/**", "/api/price/**"
    };


    private final AuthTokenRedisRepository authTokenRedisRepository;
    @Override
    public void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws IOException, ServletException {
        String authorizationHeader = request.getHeader("Authorization");
        //JWT 헤더가 있을 경우

        if (isPermitAllPath(request.getRequestURI())) {
            log.info(request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            //JWT 유효성 검증
            try {
                jwtTokenProvider.validateToken(token);

                String username = jwtTokenProvider.extractUsername(token);
                //유저와 토큰 일치 시 userDetails 생성
                UserDetails userDetails = freshBidUserDetailsService.loadUserByUsername(username);

                if (userDetails != null) {
                    //UserDetails, Password, Role -> 접근 권한 인증 Token 생성
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                    //현재 Request의 Security Context에 접근 권한 설정
                    SecurityContextHolder.getContext()
                        .setAuthentication(usernamePasswordAuthenticationToken);
                    log.info("컨텍스트 설정됨: {}", userDetails.getUsername());
                }
            } catch (ExpiredJwtException e) {
                log.info("토큰 만료, RefreshToken을 통해 새 accessToken 발행", e);
                String refreshToken = resolveTokenFromCookie(request);
                String username = jwtTokenProvider.extractUsername(refreshToken);

                if(refreshToken != null && authTokenRedisRepository.isRefreshTokenValid(username, refreshToken)) {
                    String newAccessToken = jwtTokenProvider.generateAccessToken(username);
                    //newAccessToken Response Header에 저장
                    response.setHeader("Authorization", "Bearer "+newAccessToken);

                    // 컨텍스트 재설정
                    UserDetails userDetails = freshBidUserDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
            catch (SecurityException | MalformedJwtException |UnsupportedJwtException | IllegalArgumentException e) {
                log.info("Invalid JWT Token", e);
                sendErrorResponse(response);
                return;
            }
        } else {
            // 토큰이 없으면 그냥 다음 필터로 넘김 (인증이 필요한 경로는 Spring Security에서 처리)
            log.debug("Authorization 헤더가 없거나 Bearer 토큰이 아님: {}", request.getRequestURI());
        }

        filterChain.doFilter(request, response); //다음 필터로 넘김
    }

    private boolean isPermitAllPath(String requestURI) {
        return Arrays.stream(NO_AUTH_PATTERNS)
            .anyMatch(pattern -> requestURI.matches(pattern.replace("/**", "/.*")));
    }

    private void sendErrorResponse(HttpServletResponse response) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();

        CommonResponse<Void> errorResponse = CommonResponse.<Void>builder()
            .success(false)
            .message("Internal Server Error")
            .data(null)
            .build();

        response.setStatus(401);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String responseBody = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(responseBody);
    }

    private String resolveTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if(cookies != null) {
            for(Cookie cookie: cookies) {
                if("RefreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
