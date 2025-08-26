package FreshBid.back.dto.auction;

import FreshBid.back.dto.product.ProductResponseDto;
import FreshBid.back.entity.Auction;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "경매 조회 응답 DTO")
@Getter
@Setter
@NoArgsConstructor
public class AuctionResponseDto {

    @Schema(description = "경매 고유 ID", example = "1")
    private Long id;

    @Schema(description = "경매 시작 가격 (원)", example = "10000")
    private long startPrice;

    @Schema(description = "경매 수량 (개)", example = "5")
    private int amount;

    @Schema(description = "경매 상태", example = "SCHEDULED", allowableValues = { "SCHEDULED", "IN_PROGRESS",
            "ENDED", "FAILED" })
    private String status;

    @Schema(description = "경매 등록 일시", example = "2024-12-01T09:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "경매 상품 정보")
    private ProductResponseDto product;

    public static AuctionResponseDto from(Auction auction) {
        AuctionResponseDto dto = new AuctionResponseDto();

        dto.id = auction.getId();
        dto.startPrice = auction.getStartPrice();
        dto.amount = auction.getAmount();
        dto.status = auction.getStatus().name();
        dto.createdAt = auction.getCreatedAt();
        dto.product = ProductResponseDto.toDto(auction.getProduct());
        return dto;
    }
}
