package FreshBid.back.service;

import FreshBid.back.dto.bid.BidRequestDto;
import FreshBid.back.dto.bid.BidResponseDto;
import FreshBid.back.dto.bid.BidStatusDto;
import FreshBid.back.entity.User;

public interface BidService {

    void createBid(Long auctionId, User user, Long bidPrice);

    void createBid(Long auctionId, User user, BidRequestDto bidRequestDto);

    BidStatusDto getBidStatus(Long auctionId, int limit);

    BidResponseDto finalizeBid(Long auctionId);
}