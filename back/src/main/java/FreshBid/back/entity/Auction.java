package FreshBid.back.entity;

import FreshBid.back.dto.auction.AuctionRequestDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "auction")
@Getter
@Setter
@NoArgsConstructor
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "live_id", nullable = false)
    private Live live;

    @Column(name = "start_price", nullable = false)
    private long startPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "amount", nullable = false)
    private int amount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // 아래 순서대로 정렬되므로 수정시 주의
    public enum Status {
        SCHEDULED,
        IN_PROGRESS,
        ENDED,
        FAILED
    }

    // TODO : Product 개발 완료 후 주석 해제
    public static Auction of(AuctionRequestDto auctionRequestDto, Product product, Live live) {
        Auction auction = new Auction();

        auction.startPrice = auctionRequestDto.getStartPrice();
        auction.amount = auctionRequestDto.getAmount();
        auction.status = Status.SCHEDULED;
        auction.product = product;
        auction.live = live;

        return auction;
    }
}
