package FreshBid.back.dto.product;

import FreshBid.back.entity.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Schema(description = "상품 등록 요청 DTO")
@Getter @Setter @NoArgsConstructor
public class ProductCreateRequestDto {

    @Schema(description = "상품명", example = "신선한 사과")
    private String name;

    @Schema(description = "원산지", example = "국내산")
    private String origin;

    @Schema(description = "중량 (kg)", example = "1.500")
    private BigDecimal weight;

    @Schema(description = "대표 이미지 file", example = "https://example.com/apple.jpg")
    private MultipartFile reprImgSrc;

    @Schema(description = "상품 설명", example = "제철에 수확한 신선한 사과입니다.")
    private String description;

    @Schema(description = "카테고리 ID", example = "3")
    private Integer categoryId;

    @Schema(description = "등급", example = "상", allowableValues = {"특","상","중","하"})
    private Product.Grade grade;
}
