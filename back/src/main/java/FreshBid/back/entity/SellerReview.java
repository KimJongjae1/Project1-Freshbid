package FreshBid.back.entity;

import FreshBid.back.dto.SellerReview.SellerReviewCreateRequestDto;
import FreshBid.back.dto.SellerReview.SellerReviewUpdateRequestDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "seller_review")
@Getter
@NoArgsConstructor
public class SellerReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 대댓글
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "super_id")
    private SellerReview superReview;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 작성자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller; // 판매자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false, columnDefinition = "BIT")
    private int rate;

    @Column(name = "review_image", length = 100)
    private String reviewImage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static SellerReview from(SellerReviewCreateRequestDto dto, User user, Order order, SellerReview superReview) {
        return SellerReview.of(
                user,
                order.getSeller(),  // seller는 order에서 가져오는 게 일관성 있음
                order,
                dto.getContent(),
                dto.getRate(),
                dto.getReviewImage(),
                superReview
        );
    }

    public static SellerReview of(User user, User seller, Order order, String content, int rate, String reviewImage, SellerReview superReview) {
        SellerReview review = new SellerReview();
        review.user = user;
        review.seller = seller;
        review.order = order;
        review.content = content;
        review.rate = rate;
        review.reviewImage = reviewImage;
        review.superReview = superReview;
        return review;
    }

    public void update(SellerReviewUpdateRequestDto dto) {
        this.content = dto.getContent();
        this.rate = dto.getRate();
        this.reviewImage = dto.getReviewImage();
    }

    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}
