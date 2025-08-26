package FreshBid.back.controller;

import FreshBid.back.dto.user.LoginRequestDto;
import FreshBid.back.dto.user.SignupRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "사용자 인증 API", description = "로그인, 회원가입을 위한 API입니다.")
@RequestMapping("/auth")
public interface AuthController {

        @Operation(summary = "회원가입", description = "각종 정보들을 받아 회원가입을 진행합니다.")
        @ApiResponses({ @ApiResponse(responseCode = "200", description = "회원가입 성공"),
                        @ApiResponse(responseCode = "400", description = "중복 아이디, 닉네임 에러"),
                        @ApiResponse(responseCode = "500", description = "인터넷 서버 오류") })
        @PostMapping("/signup")
        ResponseEntity<?> signup(@RequestBody SignupRequestDto requestDto);

        @Operation(summary = "로그인", description = "아이디, 비밀번호를 받아 로그인합니다.")
        @ApiResponses({ @ApiResponse(responseCode = "200", description = "로그인 성공"),
                        @ApiResponse(responseCode = "404", description = "아이디 또는 비밀번호 에러"),
                        @ApiResponse(responseCode = "500", description = "인터넷 서버 오류") })
        @PostMapping("/login")
        ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto requestDto);
}
