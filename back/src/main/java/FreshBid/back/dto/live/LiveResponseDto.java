package FreshBid.back.dto.live;

import FreshBid.back.dto.auction.AuctionResponseDto;
import FreshBid.back.entity.Live;
import FreshBid.back.entity.Live.LiveStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Live 및 경매 조회 응답 DTO")
@Getter
@Setter
@NoArgsConstructor
public class LiveResponseDto {

    @Schema(description = "Live 고유 ID", example = "1")
    private Long id;

    @Schema(description = "Live 진행 판매자 정보")
    private SellerResponseDto seller;

    @Schema(description = "Live 제목", example = "신선한 농산물 라이브 경매")
    private String title;
    
    @Schema(description = "Live 메인 사진", example = "https://~~")
    private String reprImgSrc;
    
    @Schema(description = "Live 시작 일시", example = "2024-12-01T10:00:00")
    private LocalDateTime startDate;

    @Schema(description = "Live 종료 일시", example = "2024-12-01T12:00:00")
    private LocalDateTime endDate;

    @Schema(description = "Live에서 진행되는 경매 목록")
    private List<AuctionResponseDto> auctions;

    @Schema(description = "Live 상태", example = "SCHEDULED",
        allowableValues = {"SCHEDULED", "IN_PROGRESS", "ENDED"})
    private LiveStatus liveStatus;

    public static LiveResponseDto from(Live live) {
        LiveResponseDto dto = new LiveResponseDto();

        dto.id = live.getId();
        dto.seller = SellerResponseDto.from(live.getSeller());
        dto.title = live.getTitle();
        dto.reprImgSrc = live.getReprImgSrc();
        dto.startDate = live.getStartDate();
        dto.endDate = live.getEndDate();
        dto.liveStatus = live.getStatus();

        // @OneToMany 관계를 통해 Auction 목록 변환
        // QueryDSL Join Fetch로 이미 로드된 데이터 활용
        dto.auctions = live.getAuctions().stream()
            .map(AuctionResponseDto::from)
            .toList();

        return dto;
    }
}
