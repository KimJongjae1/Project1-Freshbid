package FreshBid.back.dto.user;

import FreshBid.back.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

//Header 로드 시 호출 -> userStore에 저장하기 위함
@Data
@Builder
@Schema(description = "userStore에 저장될 사용자 정보")
public class UserBasicResponseDto {
    private String username;
    private String nickname;
    private User.Role role;
    private String profileImage;
}
