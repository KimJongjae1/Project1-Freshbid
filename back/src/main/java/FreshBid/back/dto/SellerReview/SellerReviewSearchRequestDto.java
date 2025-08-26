package FreshBid.back.dto.SellerReview;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "판매자 후기 조회 요청 DTO")
@Getter
@Setter
@NoArgsConstructor
public class SellerReviewSearchRequestDto {

    @Schema(description = "판매자 ID", example = "3")
    private Long sellerId;

    @Schema(description = "페이지 번호 (0부터 시작)", example = "0")
    @PositiveOrZero(message = "페이지 번호는 0 이상이어야 합니다.")
    private Integer page = 0;

    @Schema(description = "페이지 크기", example = "20")
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
    @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다.")
    private Integer size = 20;

    @Schema(description = "정렬 기준", example = "rate", allowableValues = {"rate", "createdAt"})
    private String sortBy = "createdAt";

    @Schema(description = "정렬 방향", example = "DESC", allowableValues = {"ASC", "DESC"})
    private String sortDirection = "DESC";
}
