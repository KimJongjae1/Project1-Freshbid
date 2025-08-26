package FreshBid.back.exception;

import FreshBid.back.dto.common.CommonResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<CommonResponse<Void>> handleDuplicateUserException(
        DuplicateUserException e) {
        log.warn("중복 사용자 예외 발생: {}", e.getMessage());
        CommonResponse<Void> response = CommonResponse.<Void>builder()
            .success(false)
            .message(e.getMessage())
            .data(null)
            .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * @param ex RequestDto의 유효성 Annotation 관련 Exception
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        log.warn("유효성 검증 실패: {}", errorMessage);

        CommonResponse<Void> response = CommonResponse.<Void>builder()
            .success(false)
            .message(errorMessage)
            .data(null)
            .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler({UsernameNotFoundException.class, BadCredentialsException.class})
    public ResponseEntity<?> handleLoginException(Exception ex) {
        log.warn("로그인 실패: {}", ex.getClass().getSimpleName());

        CommonResponse<Void> response = CommonResponse.<Void>builder()
            .success(false)
            .message("아이디 또는 비밀번호가 틀렸습니다.")
            .data(null)
            .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<CommonResponse<Void>> handleUnauthorizedException(
        UnauthorizedException e) {
        log.warn("인증 실패: {}", e.getMessage());
        CommonResponse<Void> response = CommonResponse.<Void>builder()
            .success(false)
            .message(e.getMessage())
            .data(null)
            .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<CommonResponse<Void>> handleForbiddenException(ForbiddenException e) {
        log.warn("권한 실패: {}", e.getMessage());
        CommonResponse<Void> response = CommonResponse.<Void>builder()
            .success(false)
            .message(e.getMessage())
            .data(null)
            .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<CommonResponse<Void>> handleLiveNotFoundException(NotFoundException e) {
        log.info("Live 조회 실패: {}", e.getMessage());
        CommonResponse<Void> response = CommonResponse.<Void>builder()
            .success(false)
            .message(e.getMessage())
            .data(null)
            .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CommonResponse<Void>> handleIllegalArgumentException(
        IllegalArgumentException e) {
        log.warn("비즈니스 로직 위반: {}", e.getMessage());
        CommonResponse<Void> response = CommonResponse.<Void>builder()
            .success(false)
            .message(e.getMessage())
            .data(null)
            .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * JSON 파싱 오류 처리 (잘못된 enum 값 등)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CommonResponse<Void>> handleHttpMessageNotReadableException(
        HttpMessageNotReadableException e) {
        log.warn("JSON 파싱 오류: {}", e.getMessage());

        String errorMessage = "요청 형식이 올바르지 않습니다.";

        // enum 관련 오류인 경우 더 구체적인 메시지 제공
        if (e.getMessage().contains("not one of the values accepted for Enum class")) {
            errorMessage = "유효하지 않은 경매 상태입니다.";
        }

        CommonResponse<Void> response = CommonResponse.<Void>builder()
            .success(false)
            .message(errorMessage)
            .data(null)
            .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Bean Validation 제약 조건 위반 처리
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<CommonResponse<Void>> handleConstraintViolationException(
        ConstraintViolationException e) {
        String errorMessage = e.getConstraintViolations().iterator().next().getMessage();
        log.warn("제약 조건 위반: {}", errorMessage);

        CommonResponse<Void> response = CommonResponse.<Void>builder()
            .success(false)
            .message(errorMessage)
            .data(null)
            .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<CommonResponse<Void>> handleUserNotFoundException(
        UserNotFoundException e) {
        log.warn("사용자 조회 실패: {}", e.getMessage());
        CommonResponse<Void> response = CommonResponse.<Void>builder()
            .success(false)
            .message("사용자 조회 실패")
            .data(null)
            .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<Void>> handleException(Exception e) {
        log.error("예상치 못한 오류 발생: {}", e.getMessage(), e);
        CommonResponse<Void> response = CommonResponse.<Void>builder()
            .success(false)
            .message("Internal Server Error")
            .data(null)
            .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}