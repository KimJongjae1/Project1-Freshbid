package FreshBid.back.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Schema(description = "상품 수정 요청 DTO")
@Getter @Setter @NoArgsConstructor
public class ProductUpdateRequestDto {

    @Schema(description = "상품명", example = "신선한 사과",
            allowableValues = {"null 처리 시 수정 안함"})
    private String name;

    @Schema(description = "원산지", example = "국내산")
    private String origin;

    @Schema(description = "중량 (kg)", example = "1.500")
    private BigDecimal weight;

    @Schema(description = "대표 이미지 URL", example = "https://example.com/new-apple.jpg")
    private String reprImgSrc;

    @Schema(description = "상품 설명", example = "업데이트된 설명입니다.")
    private String description;

    @Schema(description = "카테고리 ID", example = "4")
    private Integer categoryId;

    @Schema(description = "등급", example = "상", allowableValues = {"특","상","중","하"})
    private String grade;
}
