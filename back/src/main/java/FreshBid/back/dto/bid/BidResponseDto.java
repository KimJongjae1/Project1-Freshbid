package FreshBid.back.dto.bid;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "입찰 응답 DTO")
public class BidResponseDto {

    @Schema(description = "입찰 고유 ID", example = "1")
    private Long bidId;

    @Schema(description = "경매 고유 ID", example = "1")
    private Long auctionId;

    @Schema(description = "입찰자 ID")
    private Long userId;

    @Schema(description = "입찰자 닉네임")
    private String userNickName;

    @Schema(description = "입찰 가격 (원)", example = "50000")
    private Long bidPrice;

    @Schema(description = "입찰 시각", example = "2024-12-01T14:30:00")
    private LocalDateTime bidTime;
}