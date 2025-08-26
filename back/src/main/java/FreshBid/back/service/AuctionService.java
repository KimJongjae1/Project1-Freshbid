package FreshBid.back.service;

import FreshBid.back.dto.auction.AuctionRequestDto;
import FreshBid.back.dto.auction.AuctionResponseDto;
import FreshBid.back.dto.auction.AuctionSearchRequestDto;
import FreshBid.back.dto.auction.AuctionUpdateRequestDto;
import FreshBid.back.entity.Auction.Status;
import FreshBid.back.entity.Live;
import FreshBid.back.entity.Product;
import java.util.List;
import org.springframework.data.domain.Page;

public interface AuctionService {

    /**
     * 경매 페이징 조회
     *
     * @param searchRequest
     * @return
     */
    Page<AuctionResponseDto> searchAuctions(AuctionSearchRequestDto searchRequest);

    /**
     * 경매 조회
     *
     * @param auctionId 조회할 경매 ID
     * @return
     */
    AuctionResponseDto getAuction(Long auctionId);

    /**
     * 경매 정보 수정
     *
     * @param sellerId                로그인한 사용자 ID (상품 판매자 ID와 일치해야 함)
     * @param auctionId
     * @param auctionUpdateRequestDto
     */
    void updateAuction(Long sellerId, Long auctionId,
        AuctionUpdateRequestDto auctionUpdateRequestDto);

    /**
     * 경매 삭제
     *
     * @param sellerId  로그인한 사용자 ID (상품 판매자 ID와 일치해야 함)
     * @param auctionId
     */
    void deleteAuction(Long sellerId, Long auctionId);

    /**
     * 경매 생성 시 검증
     * @param auctionRequestDto 경매 생성 요청 DTO
     * @param sellerId 판매자 ID
     */
    Product validateAuctionRequest(AuctionRequestDto auctionRequestDto, Long sellerId);

    /**
     * 경매 수정 시 검증
     *
     * @param auctionUpdateRequestDto 경매 수정 요청 DTO
     * @param sellerId                판매자 ID
     */
    void validateAuctionRequest(AuctionUpdateRequestDto auctionUpdateRequestDto, Long sellerId);

    /**
     * 경매 생성
     * @param auctionRequestDto
     * @param product
     * @param live
     */
    void createAuction(AuctionRequestDto auctionRequestDto, Product product, Live live);

    /**
     * 경매 단독 생성을 위한 로직
     * @param liveId
     * @param auctionRequestDto
     * @return
     */
    AuctionResponseDto createAuctionByLiveId(AuctionRequestDto auctionRequestDto, Long liveId, Long currentUserId);


    void validateAndChangeAuctionStatus(Status asIs, Status toBe, Long sellerId, Long auctionId);
}
