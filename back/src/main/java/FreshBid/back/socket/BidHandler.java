package FreshBid.back.socket;

import FreshBid.back.dto.bid.BidResponseDto;
import FreshBid.back.dto.bid.BidStatusDto;
import FreshBid.back.entity.Auction;
import FreshBid.back.entity.Auction.Status;
import FreshBid.back.entity.User;
import FreshBid.back.repository.BidRedisRepositorySupport;
import FreshBid.back.service.AuctionService;
import FreshBid.back.service.BidService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BidHandler {

    @Autowired
    private BidService bidService;

    @Autowired
    private AuctionService auctionService;

    @Autowired
    private BidRedisRepositorySupport bidRedisRepositorySupport;

    private static final Gson gson = new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class, (com.google.gson.JsonSerializer<LocalDateTime>)
            (src, typeOfSrc, context) -> context.serialize(
                src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))).create();

    public JsonObject submitBid(User user, JsonObject jsonMessage) {
        try {
            // 1. 입찰 정보 추출
            Long auctionId = jsonMessage.get("auctionId").getAsLong();
            Long bidPrice = jsonMessage.get("bidPrice").getAsLong();

            // 2. 입찰 시도
            log.info("입찰 시도 - 사용자 ID: {}, 경매 ID: {}, 입찰가: {}", user.getId(), auctionId, bidPrice);
            bidService.createBid(auctionId, user, bidPrice);
            log.info("입찰 성공 - 사용자 ID: {}, 경매 ID: {}, 입찰가: {}", user.getId(), auctionId, bidPrice);
            return createResponse("submitBidResult", true, "입찰 성공");

        } catch (RuntimeException e) {
            // SpringBoot의 globalExceptionHandler 사용 불가 -> 직접 처리
            return createResponse("submitBidResult", false, e.getMessage());
        }
    }

    public JsonObject startAuction(Long sellerId, Long roomId, JsonObject jsonMessage) {
        try {
            // AuctionController와 동일한 처리
            Long auctionId = jsonMessage.get("auctionId").getAsLong();
            log.info("경매 시작 요청 - 경매 ID: {}", auctionId);

            auctionService.validateAndChangeAuctionStatus(Status.SCHEDULED, Status.IN_PROGRESS,
                sellerId, auctionId);
            // Redis에 경매-룸 매핑 저장
            bidRedisRepositorySupport.addActiveAuction(auctionId, roomId);

            log.info("경매방 생성 완료 - 경매 ID: {}, 판매자 ID: {}", auctionId, sellerId);
            return createResponse("startAuctionResult", true, "경매 시작 성공");

        } catch (RuntimeException e) {
            return createResponse("startAuctionResult", false, e.getMessage());
        }
    }

    public List<JsonObject> stopAuction(Long sellerId, JsonObject jsonMessage) {
        try {
            // AuctionController와 동일한 처리
            Long auctionId = jsonMessage.get("auctionId").getAsLong();
            log.info("경매 종료 요청 - 경매 ID: {}", auctionId);

            auctionService.validateAndChangeAuctionStatus(Auction.Status.IN_PROGRESS,
                Auction.Status.ENDED, sellerId, auctionId);
            log.debug("경매 종료 완료 - 경매 ID: {}", auctionId);

            BidResponseDto result = bidService.finalizeBid(auctionId);

            // Redis에서 경매-룸 매핑 삭제
            bidRedisRepositorySupport.removeActiveAuction(auctionId);
            bidRedisRepositorySupport.removeCurrentMinBidPrice(auctionId);

            List<JsonObject> response = new ArrayList<>();
            StringBuilder message = new StringBuilder("경매 종료에 성공했습니다.");
            if (result == null) {
                message.append(" 입찰 내역이 존재하지 않습니다.");
            }
            response.add(createResponse("stopAuctionResult", true, message.toString()));

            if (result != null) {
                response.add(createResponse(
                    "winningBidResult", true, "낙찰 정보"));
                response.get(1).addProperty("bidPrice", result.getBidPrice());
                response.get(1).addProperty("userId", result.getUserId());
            }

            log.info("경매방 삭제 완료 - 경매 ID: {}, 판매자 ID: {}", auctionId, sellerId);

            return response;

        } catch (RuntimeException e) {
            return List.of(createResponse("stopAuctionResult", false, e.getMessage()));
        }
    }

    public JsonObject bidStatus(BidStatusDto bidStatus) {

        JsonObject message = new JsonObject();
        message.addProperty("type", "bidStatusUpdate");
        message.addProperty("auctionId", bidStatus.getAuctionId());
        message.addProperty("status", bidStatus.getStatus());
        message.addProperty("currentHighestPrice", bidStatus.getCurrentHighestPrice());
        message.addProperty("bidListJson", gson.toJson(maskBidList(bidStatus.getBidList())));
        message.addProperty("highestBidJson",
            gson.toJson(maskBidResponse(bidStatus.getHighestBid())));

        return message;
    }

    /**
     * 브로드캐스트용 입찰 목록 마스킹 처리
     */
    private List<BidResponseDto> maskBidList(List<BidResponseDto> bidList) {
        if (bidList == null) {
            return null;
        }
        return bidList.stream()
            .map(this::maskBidResponse)
            .toList();
    }

    /**
     * 브로드캐스트용 입찰 응답 마스킹 처리
     */
    private BidResponseDto maskBidResponse(BidResponseDto bidResponse) {
        if (bidResponse == null) {
            return null;
        }
        return BidResponseDto.builder()
            .bidId(bidResponse.getBidId())
            .auctionId(bidResponse.getAuctionId())
            .userId(0L) // userId를 0으로 마스킹
            .userNickName(maskNickname(bidResponse.getUserNickName())) // 닉네임 마스킹
            .bidPrice(bidResponse.getBidPrice())
            .bidTime(bidResponse.getBidTime())
            .build();
    }

    /**
     * 닉네임 마스킹 처리 (예: "홍길동" -> "홍*동")
     */
    private String maskNickname(String nickname) {
        if (nickname == null || nickname.length() <= 2) {
            return nickname;
        }
        StringBuilder masked = new StringBuilder();
        masked.append(nickname.charAt(0)); // 첫 글자
        for (int i = 1; i < nickname.length() - 1; i++) {
            masked.append('*'); // 중간 글자들을 *로 마스킹
        }
        masked.append(nickname.charAt(nickname.length() - 1)); // 마지막 글자
        return masked.toString();
    }

    public JsonObject createResponse(String type, boolean success, String message) {
        JsonObject response = new JsonObject();
        response.addProperty("type", type);
        response.addProperty("success", success);
        response.addProperty("message", message);
        return response;
    }

}