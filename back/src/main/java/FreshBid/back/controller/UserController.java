package FreshBid.back.controller;

import FreshBid.back.dto.user.FreshBidUserDetails;
import FreshBid.back.dto.common.CommonResponse;
import FreshBid.back.dto.user.UserInfoDto;
import FreshBid.back.dto.user.UserInfoUpdateRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "User", description = "마이페이지 관련 API")
@RequestMapping("/my-page")
public interface UserController {

	@Operation(summary = "마이페이지 정보 조회", description = "인증된 사용자의 마이페이지 정보를 조회합니다.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "마이페이지 로드 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class))),
	})
	@GetMapping("")
	ResponseEntity<CommonResponse<UserInfoDto>> getUserInfo(
			@AuthenticationPrincipal FreshBidUserDetails userDetails);

	@Operation(summary = "내 정보 수정", description = "인증된 사용자의 정보를 수정합니다. Content-Type은 multipart/form-data 입니다.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "정보 수정 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class))),
			@ApiResponse(responseCode = "400", description = "DUPLICATE_ENTITY (닉네임 중복) / INVALID_FORMAT (이메일 형식 오류)", content = @Content(schema = @Schema(implementation = CommonResponse.class))),
	})
	@PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	// Content-Type 명시
	ResponseEntity<CommonResponse> updateUserInfo(
			@AuthenticationPrincipal FreshBidUserDetails userDetails,
			@Valid @ModelAttribute UserInfoUpdateRequestDto updateRequestDto);

	@Operation(summary = "로그인 사용자 정보 조회", description = "header.tsx 로드 시마다 호출하여 userStore에 기본 정보를 저장합니다.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "사용자 기본 정보 로드 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class))),
			@ApiResponse(responseCode = "404", description = "해당 id의 사용자가 없습니다.", content = @Content(schema = @Schema(implementation = CommonResponse.class))),
	})
	@GetMapping("/header")
	ResponseEntity<CommonResponse> getUserBasicInfo(@AuthenticationPrincipal FreshBidUserDetails userDetails);
}
