package FreshBid.back.util;

import com.google.gson.JsonObject;

/**
 * 나중에 리팩토링할 때 사용
 */
public class SocketUtils {

    public static JsonObject createResponse(String type, boolean success, String message) {
        JsonObject response = new JsonObject();
        response.addProperty("type", type);
        response.addProperty("success", success);
        response.addProperty("message", message);
        return response;
    }
}
