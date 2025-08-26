package FreshBid.back.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "bid_record", timeToLive = 36000) // 10시간 TTL
public class BidRecord {

    @Id
    private String bidId; // Redis용 ID (auctionId:userId:timestamp 형태)

    private Long auctionId; // auction_history.ac_id 매핑

    private Long userId; // auction_history.buy_id 매핑

    private String userNickName;

    private Long bidPrice; // auction_history.price 매핑

    private LocalDateTime bidTime; // auction_history.action_time 매핑

    @Builder.Default
    private String action = "입찰"; // auction_history.action 매핑 (기본값: '입찰')

    public static BidRecord createBid(Long auctionId, Long userId, String userNickName,
        Long bidPrice) {
        String bidId = String.format("%d:%d:%d", auctionId, userId, System.currentTimeMillis());

        return BidRecord.builder()
            .bidId(bidId)
            .auctionId(auctionId)
            .userId(userId)
            .userNickName(userNickName)
            .bidPrice(bidPrice)
            .bidTime(LocalDateTime.now())
            .action("입찰")
            .build();
    }
}