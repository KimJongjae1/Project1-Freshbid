package FreshBid.back.exception;

/**
 * 인증된 사용자가 없는 경우 throw
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}