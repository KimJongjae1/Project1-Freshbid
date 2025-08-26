package FreshBid.back.controller;

import FreshBid.back.dto.SellerReview.SellerReviewResponseDto;
import FreshBid.back.dto.SellerReview.SellerReviewSearchRequestDto;
import FreshBid.back.dto.common.CommonResponse;
import FreshBid.back.entity.Live;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "판매자 정보 페이지", description = "판매자의 기본 정보, 경매 라이브, 상품 조회, 후기 조회를 포함합니다")
@RequestMapping("/seller-info")
public interface SellerInfoController {

    @Operation(summary = "판매자의 기본 정보", description = "판매자의 기본 정보를 불러옵니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "판매자 기본 정보 로드 성공"),
            @ApiResponse(responseCode = "404", description = "해당 아이디의 판매자가 없습니다."),
            @ApiResponse(responseCode = "500", description = "인터넷 서버 오류")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getBasicInfo(@PathVariable("id") Long sellerId);

    @Operation(summary = "판매자의 상품 정보", description = "판매자가 등록한 상품 정보들을 불러옵니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "판매자 등록 상품 로드 성공"),
            @ApiResponse(responseCode = "404", description = "해당 아이디의 판매자가 없습니다."),
            @ApiResponse(responseCode = "500", description = "인터넷 서버 오류")
    })
    @GetMapping("/{id}/products")
    public ResponseEntity<?> getProducts(@PathVariable("id") Long sellerId,
                                         @RequestParam(value = "category", required = false) Integer category,
                                         @RequestParam(value = "pageNo", defaultValue = "0") Integer pageNo);

    @Operation(summary = "판매자의 경매 라이브 조회", description = "판매자가 등록한 경매 라이브 정보를 불러옵니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "판매자 경매 라이브 조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 아이디의 판매자가 없습니다."),
            @ApiResponse(responseCode = "500", description = "인터넷 서버 오류")
    })
    @GetMapping("/{id}/lives")
    public ResponseEntity<?> getLives(@PathVariable("id") Long sellerId,
                                      @RequestParam(value = "pageNo", defaultValue = "0") Integer pageNo,
                                      @RequestParam(value = "title", required = false) String title,
                                      @RequestParam(value = "isEnded", defaultValue = "false") Boolean isEnded);

    @Operation(summary = "판매자의 후기 조회", description = "판매자 후기들을 불러옵니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "판매자 후기 조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 아이디의 판매자가 없습니다."),
            @ApiResponse(responseCode = "500", description = "인터넷 서버 오류")
    })
    @GetMapping("/{id}/reviews")
    public ResponseEntity<?> getReviewsBySeller(@PathVariable("id") Long sellerId,
                                                @RequestParam(value = "pageNo", defaultValue = "0") Integer pageNo);

    @Operation(summary = "판매자의 QNA 조회", description = "판매자 QNA들을 불러옵니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "판매자 QNA 조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 아이디의 판매자가 없습니다."),
            @ApiResponse(responseCode = "500", description = "인터넷 서버 오류")
    })
    @GetMapping("/{id}/qnas")
    public ResponseEntity<?> getSellerQnas(@PathVariable("id") Long sellerId,
                                           @RequestParam(value = "pageNo", defaultValue = "0") Integer pageNo);

    @Operation(summary = "판매자 닉네임, 아이디로 검색", description = "판매자 QNA들을 불러옵니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "판매자 검색어 조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 검색어의 판매자가 없습니다."),
            @ApiResponse(responseCode = "500", description = "인터넷 서버 오류")
    })
    @GetMapping("/search")
    public ResponseEntity<?> searchSeller(@RequestParam(value = "query", required = true) String searchQuery);
}
