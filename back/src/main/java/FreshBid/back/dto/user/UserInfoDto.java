package FreshBid.back.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "마이페이지 응답 DTO")
public class UserInfoDto {

    @Schema(description = "사용자 아이디(pk)", example = "1")
    private Long id;

    @Schema(description = "아이디", example = "ssafy")
    private String username;

    @Schema(description = "닉네임", example = "김싸피")
    private String nickname;

    @Schema(description = "프로필 이미지 blob", example = "image_UUID.png")
    private String profileImage;

    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phoneNumber;

    @Schema(description = "이메일", example = "ssafy@gmail.com")
    private String email;

    @Schema(description = "주소", example = "서울 강남구 테헤란로 212")
    private String address;

    @Schema(description = "소개글", example = "항상 맛있는 과일을 파는 싸피농장")
    private String introduction;
}
