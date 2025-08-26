package FreshBid.back.controller.impl;

import FreshBid.back.controller.UserController;
import FreshBid.back.dto.user.FreshBidUserDetails;
import FreshBid.back.dto.common.CommonResponse;
import FreshBid.back.dto.user.UserBasicResponseDto;
import FreshBid.back.dto.user.UserInfoDto;
import FreshBid.back.dto.user.UserInfoUpdateRequestDto;
import FreshBid.back.entity.User;
import FreshBid.back.service.FileStorageService;
import FreshBid.back.service.UserService;
import FreshBid.back.service.impl.UserServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/my-page")
public class UserControllerImpl implements UserController {

    private final UserService userService;
    private final FileStorageService fileStorageService;
    @Override
    @GetMapping("")
    public ResponseEntity<CommonResponse<UserInfoDto>> getUserInfo(
            @AuthenticationPrincipal FreshBidUserDetails userDetails) {

        User user = userDetails.getUser();

        String imageUrl = fileStorageService.getUrl(user.getProfileImage());

        UserInfoDto userInfo = UserInfoDto.builder()
            .id(user.getId())
            .username(user.getUsername())
            .nickname(user.getNickname())
            .phoneNumber(user.getPhoneNumber())
            .profileImage(fileStorageService.convertImageUrlToBlob(imageUrl))
            .email(user.getEmail())
            .address(user.getAddress())
            .introduction(user.getIntroduction())
            .build();

        CommonResponse<UserInfoDto> response = CommonResponse.<UserInfoDto>builder()
            .success(true)
            .message("사용자 조회에 성공했습니다.")
            .data(userInfo)
            .build();

        return ResponseEntity.ok(response);
    }

    @Override
    @PutMapping("")
    public ResponseEntity<CommonResponse> updateUserInfo(
            @AuthenticationPrincipal FreshBidUserDetails userDetails,
            @Valid @ModelAttribute UserInfoUpdateRequestDto updateRequestDto) {

        Long currentUserId = userDetails.getUser().getId();

        userService.updateUserInfo(currentUserId, updateRequestDto);
        CommonResponse<UserInfoDto> response = CommonResponse.<UserInfoDto>builder()
                .success(true)
                .message("내 정보 수정 성공")
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/header")
    public ResponseEntity<CommonResponse> getUserBasicInfo(@AuthenticationPrincipal FreshBidUserDetails userDetails) {
        Long currentUserId = userDetails.getUser().getId();

        UserBasicResponseDto basicInfo = userService.getUserBasicInfo(currentUserId);

        CommonResponse<UserBasicResponseDto> response = CommonResponse.<UserBasicResponseDto>builder()
                .success(true)
                .message("내 정보 수정 성공")
                .data(basicInfo)
                .build();

        return ResponseEntity.ok(response);
    }
}
