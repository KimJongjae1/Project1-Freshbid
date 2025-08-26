package FreshBid.back.dto.live;

import FreshBid.back.entity.Live.LiveStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Live 수정 요청 DTO")
@Getter
@Setter
@NoArgsConstructor
public class LiveUpdateRequestDto {

    @Schema(description = "Live 제목", example = "신선한 농산물 라이브 경매")
    private String title;
    
    @Schema(description = "Live 메인 사진", example = "https://~~")
    private String reprImgSrc;
    
    @Schema(description = "Live 시작 일시", example = "2024-12-01T10:00:00")
    private LocalDateTime startDate;

    @Schema(description = "Live 종료 일시", example = "2024-12-01T12:00:00")
    private LocalDateTime endDate;

    @Schema(description = "라이브 상태 필터", example = "SCHEDULED",
        allowableValues = {"SCHEDULED", "IN_PROGRESS", "ENDED"})
    private LiveStatus status;
}
