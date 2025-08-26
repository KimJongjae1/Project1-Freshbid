package FreshBid.back.dto.bid;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidRequestDto {

    @NotNull(message = "입찰 가격은 필수입니다.")
    @Min(value = 1000, message = "입찰 가격은 최소 1,000원 이상이어야 합니다.")
    @Schema(description = "입찰 가격")
    private Long bidPrice;
}