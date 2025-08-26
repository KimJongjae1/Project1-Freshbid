package FreshBid.back.dto.product;

import FreshBid.back.entity.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Schema(description = "상품 조회 응답 DTO")
@Getter @Setter @NoArgsConstructor
public class ProductResponseDto {

    @Schema(description = "상품 ID", example = "101")
    private Long id;

    @Schema(description = "상품명", example = "신선한 사과")
    private String name;

    @Schema(description = "원산지", example = "국내산")
    private String origin;

    @Schema(description = "중량 (kg)", example = "1.500")
    private String weight;

    @Schema(description = "대표 이미지 URL")
    private String reprImgSrc;

    @Schema(description = "상품 설명")
    private String description;

    @Schema(description = "등급", example = "특")
    private String grade;

    @Schema(description = "등록 일시")
    private LocalDateTime createdAt;

    @Schema(description = "판매자 ID", example = "301")
    private Long userId;

    @Schema(description = "판매자 사용자명", example = "fresh_farmer")
    private String username;

    @Schema(description = "카테고리 ID", example = "3")
    private Integer categoryId;

    @Schema(description = "카테고리명", example = "과일")
    private String categoryName;

    public static ProductResponseDto toDto(Product product) {
        ProductResponseDto dto = new ProductResponseDto();
        dto.id = product.getId();
        dto.name = product.getName();
        dto.origin = product.getOrigin();
        dto.weight = product.getWeight().toString();
        dto.reprImgSrc = product.getReprImgSrc();
        dto.description = product.getDescription();
        dto.grade = product.getGrade() != null ? product.getGrade().name() : null;
        dto.createdAt = product.getCreatedAt();

        dto.userId = product.getUser().getId();
        dto.username = product.getUser().getUsername();

        dto.categoryId = product.getCategory().getId();
        dto.categoryName = product.getCategory().getName();
        return dto;
    }
}
