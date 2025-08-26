package FreshBid.back.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter // @ModelAttribute 바인딩을 위해 Setter가 필요합니다.
@Schema(description = "사용자 정보 수정 요청 DTO")
public class UserInfoUpdateRequestDto {
	
	@Schema(description = "새로운 닉네임 (unique)", example = "ssafy1")
	private String nickname;
	
	@Schema(description = "새로운 전화번호", example = "010-8765-4321")
	private String phoneNumber;

	@Schema(description = "프로필 이미지 파일")
	private MultipartFile profileImageFile;

	@Schema(description = "새로운 주소", example = "경기도 성남시 분당구")
	private String address;
	
	@Email(message = "유효한 이메일 형식이 아닙니다.")
	@Schema(description = "새로운 이메일 (이메일 정규식)", example = "new.ssafy@naver.com")
	private String email;
	
	@Schema(description = "새로운 소개글", example = "항상 산지직송 과일만을 파는 싸피 과수원입니다")
	private String introduction;
}
