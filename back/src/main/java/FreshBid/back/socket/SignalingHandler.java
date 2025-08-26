package FreshBid.back.socket;

import static FreshBid.back.util.SocketUtils.createResponse;

import FreshBid.back.dto.bid.BidStatusDto;
import FreshBid.back.dto.live.LiveUpdateRequestDto;
import FreshBid.back.dto.user.FreshBidUserDetails;
import FreshBid.back.entity.Live;
import FreshBid.back.entity.Live.LiveStatus;
import FreshBid.back.entity.User;
import FreshBid.back.entity.User.Role;
import FreshBid.back.exception.NotFoundException;
import FreshBid.back.repository.LiveRepository;
import FreshBid.back.service.LiveService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.Composite;
import org.kurento.client.HubPort;
import org.kurento.client.IceCandidate;
import org.kurento.client.IceCandidateFoundEvent;
import org.kurento.client.KurentoClient;
import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;
import org.kurento.jsonrpc.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
public class SignalingHandler extends TextWebSocketHandler {

    private static final Gson gson = new GsonBuilder().create();

    @Autowired
    private KurentoClient kurento;

    @Autowired
    private BidHandler bidHandler;

    @Autowired
    private LiveRepository liveRepository;

    @Autowired
    private LiveService liveService;

    // roomId → liveRoom 매핑
    private final ConcurrentHashMap<Long, LiveRoom> rooms = new ConcurrentHashMap<>();

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonObject jsonMessage = gson.fromJson(message.getPayload(), JsonObject.class);
        log.debug("Incoming message from session '{}': {}", session.getId(), jsonMessage);

        String type = jsonMessage.get("type").getAsString();
        Long liveId = jsonMessage.get("roomId").getAsLong(); // 방 번호 받기

        switch (type) {
            case "host":
                startHost(session, liveId, jsonMessage);
                break;
            case "participant":
                startParticipant(session, liveId, jsonMessage);
                break;
            case "onIceCandidate":
                JsonObject candidate = jsonMessage.getAsJsonObject("candidate");
                onIceCandidate(session, liveId, candidate);
                break;
            case "stop":
                stop(session, liveId);
                break;
            case "startAuction":
                startAuction(session, liveId, jsonMessage);
                break;
            case "stopAuction":
                stopAuction(session, liveId, jsonMessage);
                break;
            case "submitBid":
                submitBid(session, liveId, jsonMessage);
                break;
            case "freshCheck":
                freshCheck(session, liveId, jsonMessage);
                break;
            default:
                log.warn("Unknown message type: {}", type);
                break;
        }
    }

    /**
     * 경매 주최자(호스트) 시작
     */
    public void startHost(WebSocketSession session, Long liveId, JsonObject jsonObject) {
        String sdpOffer = jsonObject.get("sdpOffer").getAsString();

        Live live = liveRepository.findById(liveId).orElse(null);
        User user = getUserFromSession(session).getUser();

        if (live == null || Boolean.TRUE.equals(live.getIsDeleted())) {
            // 검색된 라이브 없음
            JsonObject message = createResponse("startResponse", false, "Live Not Found");
            sendMessage(session, message);
            return;
        } else if (!user.getId().equals(live.getSeller().getId())) {
            // live 등록한 판매자만 실행 가능
            JsonObject message = createResponse("startResponse", false, "라이브 시작 권한이 없습니다.");
            sendMessage(session, message);
            return;
        } else if (LocalDateTime.now().isBefore(live.getStartDate().minusHours(1)) ||
            LocalDateTime.now().isAfter(live.getEndDate())) {
            // live startTime-1h ~ endTime까지 시작 가능
            JsonObject message = createResponse("startResponse", false,
                "라이브 시작 가능한 시간이 아닙니다. 등록된 시작시간 1시간 전부터 시작 가능합니다.");
            sendMessage(session, message);
            return;
        }

        LiveRoom room = new LiveRoom();
        room.setHostSession(session);
        room.setPipeline(kurento.createMediaPipeline());

        Composite composite = new Composite.Builder(room.getPipeline()).build();
        room.setComposite(composite);

        WebRtcEndpoint hostEndpoint = new WebRtcEndpoint.Builder(room.getPipeline()).build();
        HubPort hostHubPort = new HubPort.Builder(composite).build();

        // Composite → Host 연결
        hostHubPort.connect(hostEndpoint);

        room.setHostEndpoint(hostEndpoint);
        room.setHostHubPort(hostHubPort);

        hostEndpoint.addIceCandidateFoundListener(event -> {
            JsonObject response = new JsonObject();
            response.addProperty("type", "iceCandidate");
            response.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));
            sendMessage(session, response);
        });

        String sdpAnswer = hostEndpoint.processOffer(sdpOffer);

        JsonObject response = new JsonObject();
        response.addProperty("type", "startResponse");
        response.addProperty("sdpAnswer", sdpAnswer);
        sendMessage(session, response);
        hostEndpoint.gatherCandidates();

        rooms.put(liveId, room);

        LiveUpdateRequestDto liveDto = new LiveUpdateRequestDto();
        liveDto.setStatus(LiveStatus.IN_PROGRESS);
        try {
            liveService.updateLive(user.getId(), liveId, liveDto);
        } catch (RuntimeException e) {
            log.warn("Live 상태 업데이트 실패. Live ID: {}, 변환할 상태: {}", liveId, liveDto.getStatus());
        }

        log.info("Host started for room: {}", liveId);
    }

    /**
     * 참가자 시작
     */
    public void startParticipant(WebSocketSession session, Long liveId, JsonObject jsonMessage)
        throws IOException {
        LiveRoom room = rooms.get(liveId);
        if (room == null || room.getHostSession() == null) {
            JsonObject response = new JsonObject();
            response.addProperty("type", "error");
            response.addProperty("message", "No host available in room.");
            sendMessage(session, response);
            return;
        }

        User user = getUserFromSession(session).getUser();
        if (user == null) {
            JsonObject response = createResponse("error", false, "로그인한 사용자만 참가할 수 있습니다.");
            sendMessage(session, response);
            return;
        }
        if (user.getRole() == Role.ROLE_SELLER) {
            JsonObject response = createResponse("error", false, "판매자는 라이브에 참가할 수 없습니다.");
            sendMessage(session, response);
            return;
        }

        MediaPipeline pipeline = room.getPipeline();
        WebRtcEndpoint participantEndpoint = new WebRtcEndpoint.Builder(pipeline).build();
        HubPort participantHubPort = new HubPort.Builder(room.getComposite()).build();

        // 참가자의 WebRtcEndpoint → Composite로 전송
        participantEndpoint.connect(participantHubPort);

        participantEndpoint.addIceCandidateFoundListener(event -> {
            JsonObject response = new JsonObject();
            response.addProperty("type", "iceCandidate");
            response.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));
            sendMessage(session, response);
        });

        String sdpOffer = jsonMessage.get("sdpOffer").getAsString();
        String sdpAnswer = participantEndpoint.processOffer(sdpOffer);

        // ✅ 호스트와 참가자 간 양방향 미디어 연결
        WebRtcEndpoint hostWebRtc = room.getHostEndpoint();

        // 호스트 → 참가자 연결 (호스트 미디어를 참가자가 받음)
        hostWebRtc.connect(participantEndpoint);

        JsonObject response = new JsonObject();
        response.addProperty("type", "startResponse");
        response.addProperty("sdpAnswer", sdpAnswer);
        sendMessage(session, response);
        participantEndpoint.gatherCandidates();

        // 참가자 정보를 저장
        room.getParticipants().put(session,
            new UserSession(session, session.getId(), participantEndpoint, participantHubPort));
        // 참가자 ID와 세션 매핑 정보 저장
        room.getUserIdMap().put(user.getId(), session);

        // 호스트에게 새 참가자 알림
        JsonObject notifyHost = new JsonObject();
        notifyHost.addProperty("type", "newParticipantArrived");
        notifyHost.addProperty("userId", session.getId());
        sendMessage(room.getHostSession(), notifyHost);
    }

    /**
     * ICE Candidate 처리
     */
    private void onIceCandidate(WebSocketSession session, Long liveId, JsonObject candidateJson) {
        LiveRoom room = rooms.get(liveId);
        if (room == null) {
            log.warn("Room not found for ICE candidate: {}", liveId);
            return;
        }
        boolean isHost = session.equals(room.getHostSession());

        try {
            IceCandidate candidate = new IceCandidate(
                candidateJson.get("candidate").getAsString(),
                candidateJson.get("sdpMid").getAsString(),
                candidateJson.get("sdpMLineIndex").getAsInt()
            );

            if (isHost) {
                room.getHostEndpoint().addIceCandidate(candidate);
                log.debug("Added ICE candidate to host endpoint for room: {}", liveId);
            } else {
                UserSession ps = room.getParticipants().get(session);
                if (ps != null) {
                    ps.getWebRtcEndpoint().addIceCandidate(candidate);
                    log.debug("Added ICE candidate to participant endpoint for room: {}", liveId);
                } else {
                    log.warn("Participant session not found for ICE candidate");
                }
            }
        } catch (Exception e) {
            log.error("Error adding ICE candidate: {}", e.getMessage(), e);
        }
    }

    /**
     * 연결 종료 처리
     */
    public void stop(WebSocketSession session, Long liveId) {
        LiveRoom room = rooms.get(liveId);
        if (room == null) {
            return;
        }
        boolean isHost = session.equals(room.getHostSession());
        if (isHost) {
            // 호스트 종료
            room.getParticipants().keySet().forEach(participantSession -> {
                JsonObject stopMsg = new JsonObject();
                stopMsg.addProperty("type", "leaveParticipant");
                sendMessage(participantSession, stopMsg);
            });
            room.getPipeline().release();
            rooms.remove(liveId);

            LiveUpdateRequestDto liveDto = new LiveUpdateRequestDto();
            liveDto.setStatus(LiveStatus.ENDED);
            try {
                liveService.updateLive(getUserFromSession(session).getUser().getId(), liveId,
                    liveDto);
            } catch (RuntimeException e) {
                log.warn("Live 상태 업데이트 실패. Live ID: {}, 변환할 상태: {}", liveId, liveDto.getStatus());
            }

        } else {
            // 참가자 종료
            UserSession ps = room.getParticipants().remove(session);
            if (ps != null) {
                // 연결 해제
                ps.getHubPort().disconnect(ps.getWebRtcEndpoint());

                // HubPort도 release 해야 Composite에서 제거됨
                ps.getHubPort().release();
                ps.getWebRtcEndpoint().release();

                JsonObject notifyHost = new JsonObject();
                notifyHost.addProperty("type", "leaveParticipant");
                notifyHost.addProperty("userId", ps.getSessionId());
                sendMessage(room.getHostSession(), notifyHost);

                room.getParticipants().remove(session);
            }

        }
    }

    public void startAuction(WebSocketSession session, Long roomId, JsonObject jsonMessage) {
        LiveRoom room = rooms.get(roomId);
        boolean isHost = session.equals(room.getHostSession());
        if (room == null) {
            JsonObject response = createResponse("error", false, "라이브가 시작되지 않았습니다.");
            sendMessage(session, response);
            return;
        } else if (!isHost) {
            JsonObject response = createResponse("error", false, "호스트만 경매를 시작할 수 있습니다.");
            sendMessage(session, response);
            return;
        }

        User user = getUserFromSession(session).getUser();
        JsonObject result = bidHandler.startAuction(user.getId(), roomId, jsonMessage);
        sendMessage(session, result);       // 호스트에게 처리 정보 전달
        if (result.get("success").getAsBoolean()) {
            broadcastToParticipants(room, result);
        }
    }

    public void stopAuction(WebSocketSession session, Long roomId, JsonObject jsonMessage) {
        LiveRoom room = rooms.get(roomId);
        boolean isHost = session.equals(room.getHostSession());
        if (room == null || room.getHostSession() == null) {
            JsonObject response = new JsonObject();
            response.addProperty("type", "error");
            response.addProperty("message", "No host available in room.");
            sendMessage(session, response);
            return;
        } else if (!isHost) {
            JsonObject response = createResponse("error", false, "호스트만 경매를 종료할 수 있습니다.");
            sendMessage(session, response);
            return;
        }

        User user = getUserFromSession(session).getUser();
        List<JsonObject> result = bidHandler.stopAuction(user.getId(), jsonMessage);
        JsonObject stopAuctionResult = result.get(0);

        sendMessage(session, stopAuctionResult);       // 호스트에게 처리 정보 전달
        if (stopAuctionResult.get("success").getAsBoolean()) {
            broadcastToParticipants(room, stopAuctionResult);      // 전체 참여자에게 정보 전달
        }

        // 낙찰자 정보 전달
        if (result.size() > 1) {
            JsonObject winningBidResult = result.get(1);

            // 낙찰자에게 전달
            Long winnerId = winningBidResult.get("userId").getAsLong();
            WebSocketSession winnerSession = room.getUserIdMap().get(winnerId);
            if (winnerSession != null && winnerSession.isOpen()) {
                sendMessage(winnerSession, winningBidResult);
            }

            // 호스트에게 전달
            sendMessage(session, winningBidResult);
        }
    }

    public void submitBid(WebSocketSession session, Long roomId, JsonObject jsonMessage) {
        LiveRoom room = rooms.get(roomId);
        if (room == null || room.getHostSession() == null) {
            JsonObject response = new JsonObject();
            response.addProperty("type", "error");
            response.addProperty("message", "No host available in room.");
            sendMessage(session, response);
            return;
        }

        User user = getUserFromSession(session).getUser();
        if (user.getRole() == Role.ROLE_SELLER) {
            JsonObject response = createResponse("error", false, "판매자는 경매에 참여할 수 없습니다.");
            sendMessage(session, response);
            return;
        }

        JsonObject result = bidHandler.submitBid(user, jsonMessage);
        sendMessage(session, result);   // 입찰자에게 입찰 처리 정보 전달
        if (result.get("success").getAsBoolean()) {
            sendMessage(room.getHostSession(), result); // 호스트에게 입찰 처리 정보 전달
        }
    }

    // 신선도 요청/응답을 host/participant에게 전달 (중계기 역할)
    public void freshCheck(WebSocketSession session, Long liveId, JsonObject jsonMessage) {
        LiveRoom room = rooms.get(liveId);
        if (room == null || room.getHostSession() == null) {
            JsonObject response = createResponse("error", false, "No host available in room.");
            sendMessage(session, response);
            return;
        }

        if (session.equals(room.getHostSession())) {
            log.info("FreshCheck Message (HOST) 수신 : {}", jsonMessage.toString());
            // HOST로부터 freshCheck Message 수신 == 체크 결과 메시지
            if (jsonMessage.get("freshNessResult") == null) {     // 신선도 결과가 없음
                JsonObject response = createResponse("error", false,
                    "{freshNess}에 신선도 체크 결과가 없습니다.");
                sendMessage(session, response);
                return;
            }

            int freshNess = jsonMessage.get("freshNessResult").getAsInt();
            String[] freshNessState = {"하", "중", "상", "파악불가"};

            String freshNessStr = freshNessState[(freshNess + 4) % 4];      // -1인 경우 3으로 매핑
            JsonObject response = createResponse("freshNessResult", true, freshNessStr);

            broadcastToParticipants(room, response);

        } else {
            log.info("FreshCheck Message (PARTICIPANT) 수신 : {}", jsonMessage.toString());
            // Participant로부터 freshCheck Message 수신 == 체크 요청 메시지
            JsonObject response = createResponse("freshNessRequest", true, "신선도 체크 요청 수신");
            sendMessage(room.getHostSession(), response);
        }
    }

    private void sendIceCandidate(WebSocketSession session, IceCandidateFoundEvent event) {
        if (!session.isOpen()) {
            log.warn("Skipping ICE candidate because session is closed");
            return;
        }
        JsonObject response = new JsonObject();
        response.addProperty("type", "iceCandidate");
        JsonObject candidate = JsonUtils.toJsonObject(event.getCandidate());
        response.add("candidate", candidate);
        sendMessage(session, response);
    }

    private void sendMessage(WebSocketSession session, JsonObject message) {
        try {
            if (session.isOpen()) {
                synchronized (session) { // 동기화로 동시에 write 방지
                    session.sendMessage(new TextMessage(message.toString()));
                }
            } else {
                log.warn("❌ WebSocket session already closed, skipping message");
            }
        } catch (IOException e) {
            log.error("Error sending message", e);
        }
    }

    private void broadcastToParticipants(LiveRoom room, JsonObject message) {
        for (WebSocketSession session : room.getParticipants().keySet()) {
            sendMessage(session, message);
        }
    }

    /**
     * 특정 룸에 입찰 상태 브로드캐스트 (스케줄러에서 사용)
     */
    public void broadcastBidStatusToRoom(Long roomId, BidStatusDto bidStatus) {
        LiveRoom room = rooms.get(roomId);
        if (room == null) {
            log.warn("룸을 찾을 수 없습니다 - 룸 ID: {}", roomId);
            throw new NotFoundException("룸을 찾을 수 없습니다 - 룸 ID: " + roomId);
        }
        JsonObject message = bidHandler.bidStatus(bidStatus);
        // 호스트와 모든 참가자에게 전송
        sendMessage(room.getHostSession(), message);
        broadcastToParticipants(room, message);

        log.debug("입찰 상태 브로드캐스트 완료 - 룸 ID: {}, 경매 ID: {}", roomId,
            bidStatus.getAuctionId());
    }

    /**
     * WebSocketSession에서 인증된 사용자 정보를 추출하는 헬퍼 메서드 RestController의 @AuthenticationPrincipal과 동일한 기능
     * 제공
     */
    private FreshBidUserDetails getUserFromSession(WebSocketSession session) {
        Object userDetails = session.getAttributes().get("userDetails");
        if (userDetails instanceof FreshBidUserDetails) {
            return (FreshBidUserDetails) userDetails;
        }
        log.warn("인증되지 않은 사용자의 요청 - 세션 ID: {}", session.getId());
        return null;
    }

    /**
     * 연결 설정 완료 후 사용자 인증 정보 확인
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        FreshBidUserDetails userDetails = getUserFromSession(session);
        if (userDetails != null) {
            log.info("WebSocket 연결 설정 완료 - 사용자: {}, 세션 ID: {}",
                userDetails.getUsername(), session.getId());
        } else {
            log.warn("인증되지 않은 WebSocket 연결 시도 - 세션 ID: {}", session.getId());
            session.close();
        }
        super.afterConnectionEstablished(session);
    }
}