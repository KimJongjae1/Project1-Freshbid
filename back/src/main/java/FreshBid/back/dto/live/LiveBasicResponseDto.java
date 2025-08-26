package FreshBid.back.dto.live;


import FreshBid.back.entity.Live.LiveStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LiveBasicResponseDto {
    private Long id;
    private String title;
    private String reprImgSrc;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LiveStatus status;
}
