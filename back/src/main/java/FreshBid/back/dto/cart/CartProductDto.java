package FreshBid.back.dto.cart;

import FreshBid.back.entity.Order;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder // DTO 생성을 위해 Builder 패턴을 사용합니다.
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "찜한 상품 정보 DTO")

public class CartProductDto {

    @Schema(description = "상품명", example = "사과")
    private String name;

    @Schema(description = "상품 이미지 URL", example = "https://example.com/images/apple.jpg")
    private String img;
    
    @Schema(description = "규격(kg)", example = "4")
    private BigDecimal spec;
    
    @Schema(description = "수량", example = "1")
    private Integer amount;

    @Schema(description = "농장명(판매자 닉네임)", example = "싸피 과수원")
    private String farmName;

    @Schema(description = "원산지", example = "국내산")
    private String origin;

    @Schema(description = "입찰가(낙찰가)", example = "30000")
    private Long price;

    @Schema(description = "경매상태", example = "낙찰")
    private Order.OrderStatus status; // 최종 응답은 String이므로 Enum의 설명으로 변환

    @Schema(description = "낙찰일시", example = "2025-07-18T15:14:00")
    private LocalDateTime regDate;
    
}
