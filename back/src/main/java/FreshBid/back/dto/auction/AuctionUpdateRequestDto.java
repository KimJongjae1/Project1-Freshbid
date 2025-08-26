package FreshBid.back.dto.auction;

import FreshBid.back.entity.Auction;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "경매 수정 요청 DTO")
@Getter
@Setter
@NoArgsConstructor
public class AuctionUpdateRequestDto {

    @Schema(description = "경매 시작 가격 (원)", example = "10000")
    private long startPrice;

    @Schema(description = "경매 수량 (개)", example = "5")
    private int amount;

    @Schema(description = "경매 상태", example = "SCHEDULED")
    private Auction.Status status;
}