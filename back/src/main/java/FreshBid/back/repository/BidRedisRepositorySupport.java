package FreshBid.back.repository;

import FreshBid.back.entity.BidRecord;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class BidRedisRepositorySupport {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String BID_KEY_PREFIX = "auction:bids:";
    private static final String ACTIVE_AUCTIONS_KEY = "auction:active:"; // auctionId -> roomId 매핑
    private static final String CURRENT_MIN_BID_PREFIX = "auction:current_min_bid:"; // 3초 구간별 최소 입찰가
    private static final long BID_TTL = 36000; // 10시간 (초 단위)

    public BidRedisRepositorySupport(@Qualifier("stringRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * 입찰 기록을 Redis Sorted Set에 저장
     * Score: -bidPrice (내림차순 정렬을 위해 음수 사용)
     * Member: JSON 직렬화된 BidRecord
     */
    public void saveBid(BidRecord bidRecord) {
        if (bidRecord == null) {
            throw new IllegalArgumentException("BidRecord는 null일 수 없습니다.");
        }
        if (bidRecord.getAuctionId() == null) {
            throw new IllegalArgumentException("경매 ID는 null일 수 없습니다.");
        }
        if (bidRecord.getBidPrice() == null) {
            throw new IllegalArgumentException("입찰가는 null일 수 없습니다.");
        }
        
        try {
            String key = BID_KEY_PREFIX + bidRecord.getAuctionId();
            String memberJson = objectMapper.writeValueAsString(bidRecord);
            double score = -bidRecord.getBidPrice().doubleValue(); // 내림차순을 위해 음수

            // Sorted Set에 저장
            redisTemplate.opsForZSet().add(key, memberJson, score);

            // TTL 설정
            redisTemplate.expire(key, java.time.Duration.ofSeconds(BID_TTL));

            log.debug("입찰 저장 완료 - 경매 ID: {}, 입찰가: {}",
                     bidRecord.getAuctionId(), bidRecord.getBidPrice());

        } catch (JsonProcessingException e) {
            log.error("BidRecord JSON 직렬화 실패", e);
            throw new RuntimeException("입찰 저장 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 특정 경매의 상위 N개 입찰 조회
     */
    public List<BidRecord> getTopBids(Long auctionId, int count) {
        if (auctionId == null) {
            throw new IllegalArgumentException("경매 ID는 null일 수 없습니다.");
        }
        if (count < 0) {
            throw new IllegalArgumentException("조회할 개수는 0 이상이어야 합니다.");
        }
        
        String key = BID_KEY_PREFIX + auctionId;
        Set<String> bidJsons = redisTemplate.opsForZSet().range(key, 0, count - 1);

        return convertJsonsToBidRecords(bidJsons);
    }

    /**
     * 특정 경매의 최고가 입찰 조회
     */
    public BidRecord getHighestBid(Long auctionId) {
        if (auctionId == null) {
            throw new IllegalArgumentException("경매 ID는 null일 수 없습니다.");
        }
        
        String key = BID_KEY_PREFIX + auctionId;
        Set<String> bidJsons = redisTemplate.opsForZSet().range(key, 0, 0);

        if (bidJsons == null || bidJsons.isEmpty()) {
            return null;
        }

        String bidJson = bidJsons.iterator().next();
        return convertJsonToBidRecord(bidJson);
    }

    /**
     * 특정 경매의 모든 입찰 조회 (bidPrice 내림차순)
     */
    public List<BidRecord> getAllBidsByAuctionId(Long auctionId) {
        if (auctionId == null) {
            throw new IllegalArgumentException("경매 ID는 null일 수 없습니다.");
        }
        
        String key = BID_KEY_PREFIX + auctionId;
        Set<String> bidJsons = redisTemplate.opsForZSet().range(key, 0, -1);

        return convertJsonsToBidRecords(bidJsons);
    }

    /**
     * 특정 경매의 총 입찰 수 조회
     */
    public Long getBidCount(Long auctionId) {
        if (auctionId == null) {
            throw new IllegalArgumentException("경매 ID는 null일 수 없습니다.");
        }
        
        String key = BID_KEY_PREFIX + auctionId;
        Long count = redisTemplate.opsForZSet().count(key, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        return count != null ? count : 0L;
    }

    /**
     * 특정 경매의 모든 입찰 데이터 삭제
     */
    public void deleteByAuctionId(Long auctionId) {
        if (auctionId == null) {
            throw new IllegalArgumentException("경매 ID는 null일 수 없습니다.");
        }
        
        String key = BID_KEY_PREFIX + auctionId;
        redisTemplate.delete(key);
        log.debug("경매 입찰 데이터 삭제 완료 - 경매 ID: {}", auctionId);
    }

    /**
     * JSON 배열을 BidRecord 리스트로 변환
     */
    private List<BidRecord> convertJsonsToBidRecords(Set<String> bidJsons) {
        List<BidRecord> bidRecords = new ArrayList<>();
        
        if (bidJsons == null || bidJsons.isEmpty()) {
            return bidRecords;
        }

        for (String bidJson : bidJsons) {
            if (bidJson != null && !bidJson.trim().isEmpty()) {
                BidRecord bidRecord = convertJsonToBidRecord(bidJson);
                if (bidRecord != null) {
                    bidRecords.add(bidRecord);
                }
            }
        }

        return bidRecords;
    }

    /**
     * JSON 문자열을 BidRecord 객체로 변환
     */
    private BidRecord convertJsonToBidRecord(String bidJson) {
        if (bidJson == null || bidJson.trim().isEmpty()) {
            log.warn("JSON 문자열이 null이거나 비어있습니다.");
            return null;
        }
        
        try {
            return objectMapper.readValue(bidJson, BidRecord.class);
        } catch (JsonProcessingException e) {
            log.error("BidRecord JSON 역직렬화 실패: {}", bidJson, e);
            return null;
        }
    }

    // ================ 경매-룸 매핑 관리 ================

    /**
     * 진행 중인 경매 등록 (auctionId -> roomId 매핑)
     */
    public void addActiveAuction(Long auctionId, Long roomId) {
        if (auctionId == null || roomId == null) {
            throw new IllegalArgumentException("경매 ID와 룸 ID는 null일 수 없습니다.");
        }
        
        redisTemplate.opsForHash().put(ACTIVE_AUCTIONS_KEY, auctionId.toString(), roomId.toString());
        log.debug("활성 경매 등록 - 경매 ID: {}, 룸 ID: {}", auctionId, roomId);
    }

    /**
     * 경매 종료 시 매핑 삭제
     */
    public void removeActiveAuction(Long auctionId) {
        if (auctionId == null) {
            throw new IllegalArgumentException("경매 ID는 null일 수 없습니다.");
        }
        
        redisTemplate.opsForHash().delete(ACTIVE_AUCTIONS_KEY, auctionId.toString());
        log.debug("활성 경매 삭제 - 경매 ID: {}", auctionId);
    }

    /**
     * 모든 진행 중인 경매와 roomId 매핑 조회
     */
    public Map<Long, Long> getAllActiveAuctions() {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(ACTIVE_AUCTIONS_KEY);
        
        if (entries.isEmpty()) {
            return new HashMap<>();
        }
        
        return entries.entrySet().stream()
            .collect(Collectors.toMap(
                entry -> Long.parseLong(entry.getKey().toString()),
                entry -> Long.parseLong(entry.getValue().toString())
            ));
    }

    // ================ 3초 구간별 최소 입찰가 관리 ================

    /**
     * 현재 3초 구간의 최소 입찰가 설정 (스케줄러에서 사용)
     */
    public void setCurrentMinBidPrice(Long auctionId, Long minPrice) {
        if (auctionId == null || minPrice == null) {
            throw new IllegalArgumentException("경매 ID와 최소 입찰가는 null일 수 없습니다.");
        }
        
        String key = CURRENT_MIN_BID_PREFIX + auctionId;
        redisTemplate.opsForValue().set(key, minPrice.toString(), java.time.Duration.ofSeconds(BID_TTL));
        log.debug("현재 최소 입찰가 설정 - 경매 ID: {}, 최소 입찰가: {}", auctionId, minPrice);
    }

    /**
     * 현재 3초 구간의 최소 입찰가 조회 (입찰 시 사용)
     */
    public Long getCurrentMinBidPrice(Long auctionId) {
        if (auctionId == null) {
            throw new IllegalArgumentException("경매 ID는 null일 수 없습니다.");
        }
        
        String key = CURRENT_MIN_BID_PREFIX + auctionId;
        String minPriceStr = redisTemplate.opsForValue().get(key);
        
        if (minPriceStr == null || minPriceStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            return Long.parseLong(minPriceStr);
        } catch (NumberFormatException e) {
            log.error("최소 입찰가 파싱 실패 - 경매 ID: {}, 값: {}", auctionId, minPriceStr, e);
            return null;
        }
    }

    /**
     * 경매 종료 시 최소 입찰가 정보 삭제
     */
    public void removeCurrentMinBidPrice(Long auctionId) {
        if (auctionId == null) {
            throw new IllegalArgumentException("경매 ID는 null일 수 없습니다.");
        }
        
        String key = CURRENT_MIN_BID_PREFIX + auctionId;
        redisTemplate.delete(key);
        log.debug("현재 최소 입찰가 삭제 - 경매 ID: {}", auctionId);
    }
}