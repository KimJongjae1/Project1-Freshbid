package FreshBid.back.dto.auction;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "경매 등록 요청 DTO")
@Getter
@Setter
@NoArgsConstructor
public class AuctionRequestDto {

    @Schema(description = "경매에 출품할 상품")
    private long productId;

    @Schema(description = "경매 시작 가격 (원)", example = "10000")
    private long startPrice;

    @Schema(description = "경매 수량 (개)", example = "5")
    private int amount;
}