package FreshBid.back.config;

import FreshBid.back.filter.JwtAuthenticationFilter;
import FreshBid.back.repository.AuthTokenRedisRepository;
import FreshBid.back.service.FreshBidUserDetailsService;
import FreshBid.back.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    private final FreshBidUserDetailsService freshBidUserDetailsService;

    private final AuthTokenRedisRepository authTokenRedisRepository;
    private static String[] NO_AUTH_URL = {"/auth/**", "/swagger-ui/**", "/v3/api-docs/**", "index.html", "/call/**", "/test-image", "/auction/live/**", "/price/**" };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        // 같은 Origin? : URL + PORT
        // CORS: 브라우저 정책 URL + PORT가 다르면? 허가 안됨. --> 그래서 허용
        httpSecurity.cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정
                .csrf(AbstractHttpConfigurer::disable) // CSRF: XSS랑 혼동 X
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Session 사용 안함
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, freshBidUserDetailsService, authTokenRedisRepository),
                        UsernamePasswordAuthenticationFilter.class)
                .formLogin(AbstractHttpConfigurer::disable) // Spring Security 기본 로그인 비활성화.
            .authorizeHttpRequests(auth -> auth
                // 1. OPTIONS 메서드는 항상 허용 (CORS preflight 요청용)
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // 2. 완전 공개 접근
                .requestMatchers(SecurityUrlConfig.PUBLIC_URLS).permitAll()

                // 3. 조회 전용 공개 (GET만)
                .requestMatchers(HttpMethod.GET, SecurityUrlConfig.PUBLIC_READ_URLS).permitAll()

                // 4. 판매자/관리자 전용 - 수정/삭제/생성
                .requestMatchers(HttpMethod.POST, SecurityUrlConfig.SELLER_URLS)
                .hasAnyRole("SELLER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, SecurityUrlConfig.SELLER_URLS)
                .hasAnyRole("SELLER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, SecurityUrlConfig.SELLER_URLS)
                .hasAnyRole("SELLER", "ADMIN")

                // 5. 관리자 전용
                .requestMatchers(HttpMethod.POST, SecurityUrlConfig.ADMIN_URLS).hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, SecurityUrlConfig.ADMIN_URLS).hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, SecurityUrlConfig.ADMIN_URLS).hasRole("ADMIN")

                // 6. 인증 필요한 URLs
                .requestMatchers(SecurityUrlConfig.AUTHENTICATED_URLS).authenticated()

                // 7. 나머지 요청은 인증 필요
                .anyRequest().authenticated()
            )
            .httpBasic(AbstractHttpConfigurer::disable);// Spring Security 인증 메커니즘 비활성화.


        return httpSecurity.build();
    }
    
    @Value("${FRONT_URL}")
    private String frontUrl;
    // Preflight 요청 OPTIONS Method: GET, POST, PUT ... 를 확인
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(frontUrl, "http://localhost:8080")); // 프론트엔드와 테스트 포트 허용
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")); // 허용할 HTTP 메서드
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With")); // 허용할 헤더
        configuration.setExposedHeaders(Arrays.asList("Authorization")); // 브라우저에 실제로 전송할 헤더
        configuration.setAllowCredentials(true); // 자격 증명 허용 (쿠키 등)
        configuration.setMaxAge(3600L); // Preflight 요청 결과를 캐시하는 시간 (초)

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 URL에 위의 CORS 설정 적용

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
