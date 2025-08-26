package FreshBid.back.socket;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;
import org.kurento.client.Composite;
import org.kurento.client.HubPort;
import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;
import org.springframework.web.socket.WebSocketSession;

@Data
public class LiveRoom {

    private WebSocketSession hostSession;
    private WebRtcEndpoint hostEndpoint;
    private HubPort hostHubPort;
    private MediaPipeline pipeline;
    private Composite composite;
    private Map<WebSocketSession, UserSession> participants = new ConcurrentHashMap<>();
    private Map<Long, WebSocketSession> userIdMap = new ConcurrentHashMap<>();
}
