package FreshBid.back.dto.SellerReview;

import FreshBid.back.entity.SellerReview;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Schema(description = "판매자 후기 응답 DTO")
@Getter
@Setter
@NoArgsConstructor
public class SellerReviewResponseDto {

    @Schema(description = "후기 ID", example = "1")
    private Long id;

    @Schema(description = "작성자 ID", example = "10")
    private Long userId;

    @Schema(description = "작성자 닉네임", example = "홍길동")
    private String nickname;

    @Schema(description = "판매자 ID", example = "3")
    private Long sellerId;

    @Schema(description = "주문 ID", example = "100")
    private Long orderId;

    @Schema(description = "후기 내용", example = "정말 만족스러운 거래였습니다.")
    private String content;

    @Schema(description = "별점 (1~5)", example = "5")
    private int rate;

    @Schema(description = "후기 이미지 경로", example = "/images/review.jpg")
    private String reviewImage;

    @Schema(description = "부모 리뷰 ID", example = "null")
    private Long superId;

    @Schema(description = "작성일시", example = "2025-08-04T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시", example = "2025-08-05T10:00:00")
    private LocalDateTime updatedAt;

    @Schema(description = "삭제 여부", example = "false")
    private boolean deleted;

    public static SellerReviewResponseDto from(SellerReview review) {
        SellerReviewResponseDto dto = new SellerReviewResponseDto();
        dto.id = review.getId();
        dto.userId = review.getUser().getId();
        dto.nickname = review.getUser().getNickname();
        dto.sellerId = review.getSeller().getId();
        dto.orderId = review.getOrder().getId();
        dto.content = review.getContent();
        dto.rate = review.getRate();
        dto.reviewImage = review.getReviewImage();
        dto.superId = review.getSuperReview() != null ? review.getSuperReview().getId() : null;
        dto.createdAt = review.getCreatedAt();
        dto.updatedAt = review.getUpdatedAt();
        dto.deleted = review.isDeleted();
        return dto;
    }
}
