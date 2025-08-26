package FreshBid.back.dto.auction;

import FreshBid.back.entity.Auction;
import FreshBid.back.validation.ValidStatusList;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Schema(description = "경매 검색 요청 DTO")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class AuctionSearchRequestDto {

    @Schema(description = "페이지 번호 (0부터 시작)", example = "0")
    @PositiveOrZero(message = "페이지 번호는 0 이상이어야 합니다.")
    private Integer page = 0;

    @Schema(description = "페이지 크기", example = "20")
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
    @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다.")
    private Integer size = 20;

    @Schema(description = "판매자 ID 필터", example = "301")
    private Long sellerId;

    @Schema(description = "판매자 닉네임 검색 (부분 일치)", example = "박봉팔")
    private String sellerNickname;

    @Schema(description = "상품 카테고리 필터", example = "5")
    private Integer categoryId;

    @Schema(description = "상품명 검색 (부분 일치)", example = "사과")
    private String productName;

    @Schema(description = "최소 시작 가격 필터", example = "10000")
    @PositiveOrZero(message = "최소 시작 가격은 0 이상이어야 합니다.")
    private Long startPriceFrom;

    @Schema(description = "최대 시작 가격 필터", example = "50000")
    @PositiveOrZero(message = "최대 시작 가격은 0 이상이어야 합니다.")
    private Long startPriceTo;

    @Schema(description = "최소 수량 필터", example = "1")
    @PositiveOrZero(message = "최소 수량은 0 이상이어야 합니다.")
    private Integer amountFrom;

    @Schema(description = "최대 수량 필터", example = "100")
    @PositiveOrZero(message = "최대 수량은 0 이상이어야 합니다.")
    private Integer amountTo;

    @Schema(description = "경매 상태 필터 (다중 선택)", example = "SCHEDULED")
    @ValidStatusList
    private List<Auction.Status> statuses;

    @Schema(description = "정렬 기준", example = "status", allowableValues = {"status", "startPrice",
        "amount", "sellerNickName", "productName"})
    private String sortBy = "status";

    @Schema(description = "정렬 방향", example = "ASC", allowableValues = {"ASC", "DESC"})
    private String sortDirection = "ASC";
}