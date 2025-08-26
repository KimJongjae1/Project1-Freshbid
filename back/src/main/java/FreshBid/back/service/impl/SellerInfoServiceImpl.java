package FreshBid.back.service.impl;

import FreshBid.back.dto.SellerQna.QnaResponseDto;
import FreshBid.back.dto.SellerQna.SellerQnaResponseDto;
import FreshBid.back.dto.SellerReview.SellerReviewResponseDto;
import FreshBid.back.dto.SellerReview.SellerReviewSearchRequestDto;
import FreshBid.back.dto.live.LiveBasicResponseDto;
import FreshBid.back.dto.live.LiveResponseDto;
import FreshBid.back.dto.live.SellerLiveFilterRequestDto;
import FreshBid.back.dto.product.ProductResponseDto;
import FreshBid.back.dto.user.SellerBasicInfoDto;
import FreshBid.back.dto.user.UserSearchResponseDto;
import FreshBid.back.entity.SellerReview;
import FreshBid.back.exception.NotFoundException;
import FreshBid.back.repository.*;
import FreshBid.back.service.FileStorageService;
import FreshBid.back.service.SellerInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerInfoServiceImpl implements SellerInfoService {

    private final UserRepositorySupport userRepositorySupport;
    private final LiveRepositorySupport liveRepositorySupport;
    private final ProductRepositorySupport productRepositorySupport;
    private final SellerReviewRepositorySupport sellerReviewRepositorySupport;
    private final SellerQnaRepositorySupport sellerQnaRepositorySupport;
    private final SellerBookmarkRepositorySupport sellerBookmarkRepositorySupport;
    private final FileStorageService fileStorageService;

    @Override
    public SellerBasicInfoDto getBasicInfo(Long userId){
        SellerBasicInfoDto basicInfo = userRepositorySupport.getSellerBasicInfoById(userId)
                .orElseThrow(() -> new NotFoundException("해당 판매자가 존재하지 않습니다."));

        //bookmark개수 따로 가져오기
        Long bookmarkCount = sellerBookmarkRepositorySupport.getSellerBookmarkCountById(userId);
        basicInfo.setBookmarkCount(bookmarkCount);
        return basicInfo;
    }

    @Override
    public Page<LiveBasicResponseDto> getLives(SellerLiveFilterRequestDto sellerLiveFilterRequestDto){
        if(userRepositorySupport.getSellerById(sellerLiveFilterRequestDto.getSellerId()).isEmpty()) {
            throw new NotFoundException("해당 판매자가 존재하지 않습니다.");
        }

        Page<LiveBasicResponseDto> lives = liveRepositorySupport.searchSellerLives(sellerLiveFilterRequestDto);
        for(LiveBasicResponseDto dto: lives.getContent()) {
            String base64Image = fileStorageService.convertImageUrlToBlob(fileStorageService.getUrl(dto.getReprImgSrc()));
            dto.setReprImgSrc(base64Image);
        }
        return lives;
    }

    @Override
    public Page<ProductResponseDto> getProductsBySellerId(Long sellerId, Integer category, Integer page) {
        if(userRepositorySupport.getSellerById(sellerId).isEmpty()) {
            throw new NotFoundException("해당 판매자가 존재하지 않습니다.");
        }
        Pageable pageable = PageRequest.of(page, 8);

        return productRepositorySupport.getProductsBySellerId(sellerId, category, pageable);
    }

    @Override
    public Page<SellerReviewResponseDto> getSellerReviewsBySellerId(Long sellerId, Integer page) {
        if(userRepositorySupport.getSellerById(sellerId).isEmpty()) {
            throw new NotFoundException("해당 판매자가 존재하지 않습니다.");
        }
        SellerReviewSearchRequestDto request = new SellerReviewSearchRequestDto();

        request.setSellerId(sellerId);
        request.setPage(page);

        Page<SellerReview> reviews = sellerReviewRepositorySupport.search(request);
        return reviews.map(SellerReviewResponseDto::from);
    }

    @Override
    public Page<QnaResponseDto> getSellerQnasBySellerId(Long sellerId, Integer page) {
        if(userRepositorySupport.getSellerById(sellerId).isEmpty()) {
            throw new NotFoundException("해당 판매자가 존재하지 않습니다.");
        }
        Pageable pageable = PageRequest.of(page, 8);

        return sellerQnaRepositorySupport.searchBySellerId(sellerId, pageable);
    }

    @Override
    public List<UserSearchResponseDto> searchSellerByQuery(String searchQuery) {

        List<UserSearchResponseDto> searchedSellers =  userRepositorySupport.searchSellerByQuery(searchQuery);
        //profileImage -> minio URL -> base64 image 형태로 변환
        for(UserSearchResponseDto dto: searchedSellers) {
            String base64Image = fileStorageService.convertImageUrlToBlob(fileStorageService.getUrl(dto.getProfileImage()));
            dto.setProfileImage(base64Image);
        }

        return searchedSellers;
    }
}
