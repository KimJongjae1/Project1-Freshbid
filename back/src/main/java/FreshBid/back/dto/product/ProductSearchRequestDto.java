package FreshBid.back.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Schema(description = "상품 검색 요청 DTO")
@Getter @Setter @NoArgsConstructor
public class ProductSearchRequestDto {

    @Schema(description = "페이지 번호 (0부터 시작)", example = "0")
    private Integer page = 0;

    @Schema(description = "페이지 크기", example = "20")
    private Integer size = 20;

    @Schema(description = "카테고리 ID 필터", example = "3")
    private Integer categoryId;

    @Schema(description = "상품명 검색 (부분 일치)", example = "사과")
    private String name;

    @Schema(description = "최소 중량 (kg)", example = "1.000")
    private BigDecimal minWeight;

    @Schema(description = "최대 중량 (kg)", example = "5.000")
    private BigDecimal maxWeight;

    @Schema(description = "등급 필터", example = "중", allowableValues = {"특","상","중","하"})
    private String grade;

    @Schema(description = "정렬 기준", example = "createdAt",
            allowableValues = {"createdAt","name","weight"})
    private String sortBy = "createdAt";

    @Schema(description = "정렬 방향", example = "DESC", allowableValues = {"ASC","DESC"})
    private String sortDirection = "DESC";
}
