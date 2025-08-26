package FreshBid.back.dto.Order;

import FreshBid.back.dto.live.SellerResponseDto;
import FreshBid.back.dto.product.ProductResponseDto;
import FreshBid.back.entity.Order;
import FreshBid.back.entity.Order.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "주문 상세 응답 DTO")
public class OrderDetailResponseDto {

    @Schema(description = "주문 ID", example = "1")
    private Long id;

    @Schema(description = "경매 ID", example = "100")
    private Long auctionId;

    @Schema(description = "주문 상태", example = "WAITING")
    private OrderStatus status;

    @Schema(description = "낙찰가격", example = "50000")
    private long price;

    @Schema(description = "수량", example = "1")
    private int amount;

    @Schema(description = "상품 정보")
    private ProductResponseDto product;

    @Schema(description = "주문 생성일시", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "주문 수정일시", example = "2024-01-15T15:20:00")
    private LocalDateTime updatedAt;

    @Schema(description = "현재 상태에서 변경 가능한 상태 목록", example = "[\"PAID\", \"CANCELLED\"]")
    private List<OrderStatus> allowedTransitions;

    public static OrderDetailResponseDto from(Order order, List<OrderStatus> allowedTransitions) {
        return OrderDetailResponseDto.builder()
                .id(order.getId())
                .auctionId(order.getAuctionHistory().getAuctionId())
                .status(order.getStatus())
                .price(order.getAuctionHistory().getPrice())
                .amount(order.getAuction().getAmount())
                .product(ProductResponseDto.toDto(order.getAuction().getProduct()))
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .allowedTransitions(allowedTransitions)
                .build();
    }

    public static OrderDetailResponseDto from(Order order, ProductResponseDto productDto, List<OrderStatus> allowedTransitions) {
        return OrderDetailResponseDto.builder()
                .id(order.getId())
                .auctionId(order.getAuctionHistory().getAuctionId())
                .status(order.getStatus())
                .price(order.getAuctionHistory().getPrice())
                .amount(order.getAuction().getAmount())
                .product(productDto)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .allowedTransitions(allowedTransitions)
                .build();
    }
}