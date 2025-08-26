package FreshBid.back.controller.impl;

import FreshBid.back.controller.SellerInfoController;
import FreshBid.back.dto.SellerQna.QnaResponseDto;
import FreshBid.back.dto.SellerReview.SellerReviewResponseDto;
import FreshBid.back.dto.SellerReview.SellerReviewSearchRequestDto;
import FreshBid.back.dto.common.CommonResponse;
import FreshBid.back.dto.live.LiveBasicResponseDto;
import FreshBid.back.dto.user.UserSearchResponseDto;
import FreshBid.back.entity.Live.LiveStatus;
import FreshBid.back.dto.live.SellerLiveFilterRequestDto;
import FreshBid.back.dto.product.ProductResponseDto;
import FreshBid.back.dto.user.SellerBasicInfoDto;
import FreshBid.back.service.FileStorageService;
import FreshBid.back.service.SellerInfoService;
import FreshBid.back.service.SellerReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SellerInfoControllerImpl implements SellerInfoController {

    private final SellerInfoService sellerInfoService;
    private final FileStorageService fileStorageService;
    @Override
    public ResponseEntity<?> getBasicInfo(Long sellerId) {
        SellerBasicInfoDto basicInfo = sellerInfoService.getBasicInfo(sellerId);
        CommonResponse<SellerBasicInfoDto> response
                = CommonResponse.<SellerBasicInfoDto>builder()
                .success(true).message("판매자 기본 정보 조회 완료.")
                .data(basicInfo).build();

        return ResponseEntity.ok(response);
    }

    //TODO: 판매자 등록 상품들 조회 (Product CRUD 완료 후)
    @Override
    public ResponseEntity<?> getProducts(Long sellerId, Integer category, Integer pageNo) {
        Page<ProductResponseDto> products = sellerInfoService.getProductsBySellerId(sellerId, category, pageNo);

        for(ProductResponseDto dto: products.getContent()) {
            String imagePath = dto.getReprImgSrc();
            if(imagePath == null) continue;
            String base64Img = fileStorageService.convertImageUrlToBlob(fileStorageService.getUrl(imagePath));
            dto.setReprImgSrc(base64Img);
        }

        CommonResponse<Page<ProductResponseDto>> response
                = CommonResponse.<Page<ProductResponseDto>>builder()
                .success(true).message("판매자 판매 상품 조회 완료.")
                .data(products).build();

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<?> getLives(Long sellerId, Integer pageNo, String title, Boolean isEnded) {
        SellerLiveFilterRequestDto dto = new SellerLiveFilterRequestDto(sellerId, pageNo, 9, title, isEnded);

        Page<LiveBasicResponseDto> livePages = sellerInfoService.getLives(dto);
        CommonResponse<Page<LiveBasicResponseDto>> response
                = CommonResponse.<Page<LiveBasicResponseDto>>builder()
                .success(true).message("판매자 경매 라이브 조회 완료.")
                .data(livePages).build();

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<?> getSellerQnas(Long sellerId, Integer pageNo) {
        Page<QnaResponseDto> sellerQnas = sellerInfoService.getSellerQnasBySellerId(sellerId, pageNo);

        CommonResponse<Page<QnaResponseDto>> response
                = CommonResponse.<Page<QnaResponseDto>>builder()
                .success(true).message("판매자 문의 글 조회 완료.")
                .data(sellerQnas).build();

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<?> getReviewsBySeller(Long sellerId, Integer pageNo) {
        Page<SellerReviewResponseDto> sellerReviews = sellerInfoService.getSellerReviewsBySellerId(sellerId, pageNo);

        CommonResponse<Page<SellerReviewResponseDto>> response
                = CommonResponse.<Page<SellerReviewResponseDto>>builder()
                .success(true).message("판매자 후기 글 조회 완료.")
                .data(sellerReviews).build();

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<?> searchSeller(String searchQuery) {
        List<UserSearchResponseDto> sellersSearched = sellerInfoService.searchSellerByQuery(searchQuery);

        CommonResponse<List<UserSearchResponseDto>> response
                = CommonResponse.<List<UserSearchResponseDto>>builder()
                .success(true)
                .message("검색어 쿼리 포함 판매자 조회 완료")
                .data(sellersSearched).build();

        return ResponseEntity.ok(response);
    }
}
