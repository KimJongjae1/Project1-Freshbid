package FreshBid.back.exception;

/**
 * 인증된 사용자이지만 권한이 없는 경우 throw (403 Forbidden)
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}