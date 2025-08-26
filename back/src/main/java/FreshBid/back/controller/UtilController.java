package FreshBid.back.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "기타 API", description = "기타 도구 API입니다.")
@RequestMapping("/util")
public interface UtilController {

    /**
     * JWT 검증 및 refresh
     * <p>
     * 비즈니스 로직 없이 토큰 유효성 검사 및 재발급 필요한 경우 사용하는 더미 API
     * <p>
     * WebSocket 연결 전 유효한 토큰 필요할 때 사용
     * <p>
     * WebSecurityConfig에서 authenticated 필수
     *
     * @return
     */
    @Operation(summary = "토큰 검증", description = "Header의 JWT를 검증 및 만료된 경우 refresh합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "토큰 검증 성공"),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰"),
        @ApiResponse(responseCode = "500", description = "인터넷 서버 오류")
    })
    @GetMapping("/check-token")
    ResponseEntity<?> checkToken();
}
