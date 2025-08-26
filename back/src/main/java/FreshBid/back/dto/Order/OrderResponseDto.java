package FreshBid.back.dto.Order;

import FreshBid.back.entity.Order;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "주문 목록 응답 DTO")
public class OrderResponseDto {

    @Schema(description = "주문 ID", example = "1")
    private Long id;

    @Schema(description = "경매 ID", example = "100")
    private Long auctionId;

    @Schema(description = "주문 상태", example = "WAITING", allowableValues = {"WAITING", "PAID", "SHIPPED", "COMPLETED", "PENDING", "CANCELLED", "REFUNDED"})
    private String status;

    @Schema(description = "낙찰가격", example = "50000")
    private long price;

    @Schema(description = "수량", example = "1")
    private int amount;

    @Schema(description = "판매자 ID", example = "2")
    private Long sellerId;

    @Schema(description = "상품 ID", example = "10")
    private Long productId;

    @Schema(description = "주문 생성일시", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "주문 수정일시", example = "2024-01-15T15:20:00")
    private LocalDateTime updatedAt;

    public static OrderResponseDto from(Order order) {
        return OrderResponseDto.builder()
                .id(order.getId())
                .auctionId(order.getAuction().getId())
                .status(order.getStatus().toString())
                .price(order.getAuctionHistory().getPrice())
                .amount(order.getAuction().getAmount())
                .sellerId(order.getSeller().getId())
                .productId(order.getAuction().getProduct().getId())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
