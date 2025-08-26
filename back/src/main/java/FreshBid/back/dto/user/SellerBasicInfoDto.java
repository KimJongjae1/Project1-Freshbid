package FreshBid.back.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "판매자 기본 정보 DTO")
@Data
public class SellerBasicInfoDto {
    @Schema(description = "판매자 PRIMARY KEY ID", example = "1")
    private Long id;
    @Schema(description = "판매자 프로필 이미지 URL", example = "https://edu.ssafy.com/edu/main/index.do")
    private String profileImage;
    @Schema(description = "판매자 아이디", example = "ssafy123")
    private String username;
    @Schema(description = "닉네임", example = "김싸피")
    private String nickname;
    @Schema(description = "주소", example = "서울 강남구 테헤란로 212")
    private String address;
    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phoneNumber;
    @Schema(description = "간단 소개글", example = "안녕하세요. 반갑습니다.")
    private String introduction;
    @Schema(description = "찜 개수", example = "20")
    private Long bookmarkCount;
}
