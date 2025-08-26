package FreshBid.back.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "seller_bookmark")
@Getter
@NoArgsConstructor
public class SellerBookmark {

    @EmbeddedId
    private SellerBookmarkKey key;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(referencedColumnName = "id", name = "seller_id", nullable = false)
    @MapsId("sellerId")
    private User seller;

    //찜한 소비자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(referencedColumnName = "id", name = "user_id", nullable = false)
    @MapsId("userId")
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public static SellerBookmark of(User seller, User user) {
        SellerBookmark sellerBookmark = new SellerBookmark();

        sellerBookmark.key = new SellerBookmarkKey(seller.getId(), user.getId());
        sellerBookmark.seller = seller;
        sellerBookmark.user = user;

        return sellerBookmark;
    }
}
