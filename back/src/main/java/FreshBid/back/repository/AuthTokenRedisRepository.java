package FreshBid.back.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import javax.inject.Qualifier;
import java.time.Duration;

/**
 * Redis에 RefreshToken을 저장하기 위한 Repository class
 */
@Repository
public class AuthTokenRedisRepository {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final long TOKEN_TTL = 604800; //일주일

    private static final String TOKEN_KEY_PREFIX="auth:refresh-token:";

    public void saveToken(String username, String refreshToken) {
        String key = TOKEN_KEY_PREFIX+username;

        redisTemplate.opsForValue().set(key, refreshToken, Duration.ofSeconds(TOKEN_TTL));
    }

    //RefreshToken을 받아와서 새로운
    public boolean isRefreshTokenValid(String username, String refreshToken) {
        String key = TOKEN_KEY_PREFIX + username;
        String stored = redisTemplate.opsForValue().get(key);
        return refreshToken.equals(stored);
    }
}
