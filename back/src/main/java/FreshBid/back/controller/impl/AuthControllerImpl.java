package FreshBid.back.controller.impl;

import FreshBid.back.controller.AuthController;
import FreshBid.back.dto.common.CommonResponse;
import FreshBid.back.dto.user.LoginRequestDto;
import FreshBid.back.dto.user.SignupRequestDto;
import FreshBid.back.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthControllerImpl implements AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequestDto requestDto) {

        log.info("회원가입 요청 - 요청 아이디 : {}", requestDto.getUsername());

        Map<String, String> tokens = authService.signup(requestDto);
        String accessToken = tokens.get("accessToken");
        String refreshToken = tokens.get("refreshToken");

        // AccessToken은 HTTP Header에 담아 전송.
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

        // ✅ RefreshToken은 HttpOnly 쿠키로 설정
        ResponseCookie refreshCookie = ResponseCookie.from("RefreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ofDays(7))
                .sameSite("Strict")  // 또는 "Lax" (필요에 따라 선택)
                .build();
        httpHeaders.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        CommonResponse<Void> response = CommonResponse.<Void>builder().success(true)
                .message("회원가입에 성공했습니다.").data(null).build();
        return ResponseEntity.status(HttpStatus.OK).headers(httpHeaders).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto requestDto) {
        log.info("로그인 요청 - 요청 아이디 : {}", requestDto.getUsername());

        Map<String, String> tokens = authService.login(requestDto);
        String accessToken = tokens.get("accessToken");
        String refreshToken = tokens.get("refreshToken");

        // AccessToken은 HTTP Header에 담아 전송.
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

        // ✅ RefreshToken은 HttpOnly 쿠키로 설정
        ResponseCookie refreshCookie = ResponseCookie.from("RefreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ofDays(7))
                .sameSite("Strict")  // 또는 "Lax" (필요에 따라 선택)
                .build();
        httpHeaders.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        // return -> Server에서 Client로 데이터를 전송.
        CommonResponse<Void> response
                = CommonResponse.<Void>builder()
                .success(true)
                .message("로그인에 성공했습니다")
                .data(null)
                .build();
        return ResponseEntity.status(HttpStatus.OK).headers(httpHeaders).body(response);
    }
}
