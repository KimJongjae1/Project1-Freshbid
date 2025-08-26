package FreshBid.back.dto.bid;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "입찰 상태 조회 DTO")
public class BidStatusDto {

    @Schema(description = "경매 고유 ID", example = "1")
    private Long auctionId;

    @Schema(description = "경매 상태", example = "SCHEDULED",
        allowableValues = {"SCHEDULED", "IN_PROGRESS", "ENDED", "FAILED"})
    private String status;

    @Schema(description = "현재 최고 입찰가 (원)", example = "75000")
    private Long currentHighestPrice;

    @Schema(description = "입찰 목록")
    private List<BidResponseDto> bidList;

    @Schema(description = "최고 입찰 정보")
    private BidResponseDto highestBid;
}