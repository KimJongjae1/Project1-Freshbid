package FreshBid.back.dto.live;

import FreshBid.back.dto.auction.AuctionRequestDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Live 및 경매 등록 요청 DTO")
@Getter
@Setter
@NoArgsConstructor
public class LiveCreateRequestDto {

    @Schema(description = "Live 제목", example = "신선한 농산물 라이브 경매")
    private String title;
    
    @Schema(description = "Live 메인 사진", example = "https://~~")
    private MultipartFile imgFile;
    
    @Schema(description = "Live 시작 일시", example = "2024-12-01T10:00:00")
    private LocalDateTime startDate;

    @Schema(description = "Live 종료 일시", example = "2024-12-01T12:00:00")
    private LocalDateTime endDate;

    @Schema(description = "경매 목록을 json으로 먼저 받음", example = "2024-12-01T12:00:00")
    private String auctionsJson;

    @Schema(description = "Live에서 진행할 경매 목록")
    private List<AuctionRequestDto> auctions;
}
