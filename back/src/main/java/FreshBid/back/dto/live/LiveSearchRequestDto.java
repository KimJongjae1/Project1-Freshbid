package FreshBid.back.dto.live;

import FreshBid.back.entity.Live.LiveStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Schema(description = "Live 검색 요청 DTO")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class LiveSearchRequestDto {

    @Schema(description = "페이지 번호 (0부터 시작)", example = "0")
    private Integer page = 0;

    @Schema(description = "페이지 크기", example = "20")
    private Integer size = 20;

    @Schema(description = "판매자 ID 필터", example = "301")
    private Long sellerId;

    @Schema(description = "판매자 닉네임 검색 (부분 일치)", example = "박봉팔")
    private String sellerNickname;

    @Schema(description = "상품 카테고리 필터", example = "5")
    private Integer categoryId;

    @Schema(description = "상품명 검색 (부분 일치)", example = "사과")
    private String productName;
    
    @Schema(description = "라이브 제목 검색 (부분 일치)", example = "사과 경매")
    private String title;
    
    @Schema(description = "통합 검색 쿼리 (제목, 상품명, 판매자명 OR 검색)", example = "사과")
    private String searchQuery;
    
    @Schema(description = "Live 메인 사진", example = "https://~~")
    private String reprImgSrc;
    
    @Schema(description = "라이브 시작일 이후 필터", example = "2025-07-20T00:00:00")
    private LocalDateTime startDateFrom;

    @Schema(description = "라이브 시작일 이전 필터", example = "2025-07-25T23:59:59")
    private LocalDateTime startDateTo;

    @Schema(description = "라이브 종료일 이후 필터", example = "2025-07-20T00:00:00")
    private LocalDateTime endDateFrom;

    @Schema(description = "라이브 종료일 이전 필터", example = "2025-07-25T23:59:59")
    private LocalDateTime endDateTo;

    @Schema(description = "라이브 상태 필터 (여러개 가능)", example = "[\"SCHEDULED\", \"IN_PROGRESS\"]")
    private List<LiveStatus> statuses;

    @Schema(description = "정렬 기준", example = "startDate", allowableValues = {"startDate", "endDate",
        "title"})
    private String sortBy = "endDate";

    @Schema(description = "정렬 방향", example = "ASC", allowableValues = {"ASC", "DESC"})
    private String sortDirection = "ASC";
}