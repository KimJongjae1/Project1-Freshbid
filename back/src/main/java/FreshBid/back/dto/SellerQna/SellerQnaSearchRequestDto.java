package FreshBid.back.dto.SellerQna;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "판매자 문의 검색 요청 DTO")
@Getter
@Setter
@NoArgsConstructor
public class SellerQnaSearchRequestDto {

    @Schema(description = "판매자 ID", example = "3")
    private Long sellerId;

    @Schema(description = "검색 키워드 (내용)", example = "상품")
    private String keyword;

    @Schema(description = "페이지 번호 (0부터 시작)", example = "0")
    private int page = 0;

    @Schema(description = "페이지 크기", example = "10")
    private int size = 10;

    @Schema(description = "정렬 기준", example = "createdAt")
    private String sortBy = "createdAt";

    @Schema(description = "정렬 방향 (ASC/DESC)", example = "DESC")
    private String sortDirection = "DESC";
} 