package FreshBid.back.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 설정 클래스
 * 캐싱과 Redis 연결을 위한 구성을 담당합니다.
 */
@EnableCaching
@EnableRedisRepositories
@Configuration
public class RedisConfig {

    // Redis 서버 호스트 주소
    @Value("${spring.data.redis.host}")
    private String host;

    // Redis 서버 포트 번호
    @Value("${spring.data.redis.port}")
    private String port;

    // Redis 서버 패스워드
    @Value("${spring.data.redis.password}")
    private String password;

    /**
     * Redis 연결 팩토리를 생성합니다.
     * Lettuce 클라이언트를 사용하여 Redis 서버와의 연결을 설정합니다.
     * 
     * @return Redis 연결 팩토리 인스턴스
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // Redis 독립형 구성 설정
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(host);
        redisStandaloneConfiguration.setPort(Integer.parseInt(port));
        redisStandaloneConfiguration.setPassword(password);
        
        // Lettuce 연결 팩토리 생성 및 반환
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(redisStandaloneConfiguration);
        return connectionFactory;
    }

    /**
     * Redis 템플릿을 생성합니다.
     * Redis 데이터 조작을 위한 템플릿으로, 문자열 직렬화기를 설정합니다.
     * 
     * @return 설정된 Redis 템플릿 인스턴스
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        
        // 연결 팩토리 설정
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        
        // 키와 값 직렬화기를 문자열로 설정
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        
        return redisTemplate;
    }

}
