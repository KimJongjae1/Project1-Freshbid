package FreshBid.back.exception;

/**
 * 회원가입 시 아이디 또는 닉네임 중복일 때 throw
 */
public class DuplicateUserException extends RuntimeException {

    public DuplicateUserException(String message) {
        super(message);
    }
}
