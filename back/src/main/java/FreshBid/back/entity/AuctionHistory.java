package FreshBid.back.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "auction_history")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 단순 이관을 위해 Join 생략
    @Column(name = "ac_id", nullable = false)
    private Long auctionId;

    // 단순 이관을 위해 Join 생략
    @Column(name = "buy_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Action action;

    @Column(name = "action_time", nullable = false)
    private LocalDateTime actionTime;

    @Column(nullable = false)
    private Long price;

    @PrePersist
    public void prePersist() {
        if (this.actionTime == null) {
            this.actionTime = LocalDateTime.now();
        }
    }

    public enum Action {
        입찰, 낙찰, 포기, 결제완료
    }

    public static AuctionHistory fromBidRecord(BidRecord bidRecord) {
        return AuctionHistory.builder()
            .auctionId(bidRecord.getAuctionId())
            .userId(bidRecord.getUserId())
            .action(Action.valueOf(bidRecord.getAction()))
            .actionTime(bidRecord.getBidTime())
            .price(bidRecord.getBidPrice())
            .build();
    }
}