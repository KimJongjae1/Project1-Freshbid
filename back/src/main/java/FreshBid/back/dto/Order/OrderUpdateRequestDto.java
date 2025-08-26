package FreshBid.back.dto.Order;

import FreshBid.back.entity.Order;
import FreshBid.back.entity.Order.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "주문 상태 변경 요청 DTO")
public class OrderUpdateRequestDto {

    @NotNull
    @Schema(description = "주문 ID", example = "1", required = true)
    private Long orderId;

    @NotNull
    @Schema(description = "변경할 주문 상태", example = "PAID", required = true, 
            allowableValues = {"WAITING", "PAID", "SHIPPED", "COMPLETED", "PENDING", "CANCELLED", "REFUNDED"})
    private OrderStatus orderStatus;

    public Order toEntity() {
        Order order = new Order();
        order.setStatus(this.orderStatus);
        return order;
    }

    public static OrderUpdateRequestDto of(Long orderId, OrderStatus orderStatus) {
        return new OrderUpdateRequestDto(orderId, orderStatus);
    }
}
