package FreshBid.back.service;

import FreshBid.back.dto.SellerQna.QnaResponseDto;
import FreshBid.back.dto.SellerReview.SellerReviewResponseDto;
import FreshBid.back.dto.live.LiveBasicResponseDto;
import FreshBid.back.dto.live.LiveResponseDto;
import FreshBid.back.dto.live.SellerLiveFilterRequestDto;
import FreshBid.back.dto.product.ProductResponseDto;
import FreshBid.back.dto.user.SellerBasicInfoDto;
import FreshBid.back.dto.user.UserSearchResponseDto;
import FreshBid.back.repository.UserRepositorySupport;
import org.springframework.data.domain.Page;

import java.util.List;

public interface SellerInfoService {
    public SellerBasicInfoDto getBasicInfo(Long userId);

    public Page<LiveBasicResponseDto> getLives(SellerLiveFilterRequestDto sellerLiveFilterRequestDto);

    public Page<ProductResponseDto> getProductsBySellerId(Long sellerId, Integer category, Integer page);

    public Page<SellerReviewResponseDto> getSellerReviewsBySellerId(Long sellerId, Integer page);

    public Page<QnaResponseDto> getSellerQnasBySellerId(Long sellerId, Integer page);

    public List<UserSearchResponseDto> searchSellerByQuery(String searchQuery);
}
