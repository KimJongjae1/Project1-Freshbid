package FreshBid.back.socket;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.kurento.client.HubPort;
import org.kurento.client.WebRtcEndpoint;
import org.springframework.web.socket.WebSocketSession;

@Data
@AllArgsConstructor
public class UserSession {
    private WebSocketSession session;
    private String sessionId;
    private WebRtcEndpoint webRtcEndpoint;
    private HubPort hubPort;
}
