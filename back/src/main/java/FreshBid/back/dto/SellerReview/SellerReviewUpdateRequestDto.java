package FreshBid.back.dto.SellerReview;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "판매자 후기 수정 요청 DTO")
@Getter
@Setter
@NoArgsConstructor
public class SellerReviewUpdateRequestDto {

    @NotBlank
    @Schema(description = "후기 내용", example = "상품이 아주 만족스러워요.")
    private String content;

    @Min(1)
    @Max(5)
    @Schema(description = "별점 (1~5)", example = "4")
    private int rate;

    @Schema(description = "후기 이미지 경로", example = "/images/updated_review.jpg")
    private String reviewImage;
}
