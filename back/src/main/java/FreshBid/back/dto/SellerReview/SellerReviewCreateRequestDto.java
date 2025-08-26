package FreshBid.back.dto.SellerReview;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "판매자 후기 등록 요청 DTO")
@Getter
@Setter
@NoArgsConstructor
public class SellerReviewCreateRequestDto {

    @Schema(description = "주문 ID", example = "10")
    private Long orderId;

    @Schema(description = "판매자 ID", example = "3")
    private Long sellerId;

    @Schema(description = "부모 리뷰 ID (댓글인 경우)", example = "1")
    private Long superId;

    @NotBlank
    @Schema(description = "후기 내용", example = "배송이 빨라요")
    private String content;

    @Min(1)
    @Max(5)
    @Schema(description = "별점 (1~5)", example = "5")
    private int rate;

    @Schema(description = "후기 이미지 경로", example = "/images/review1.jpg")
    private String reviewImage;
}
