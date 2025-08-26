package FreshBid.back.dto.user;

import lombok.Data;

@Data
public class UserSearchResponseDto {
    private Long id;
    private String profileImage;
    private String username;
    private String nickname;
}
