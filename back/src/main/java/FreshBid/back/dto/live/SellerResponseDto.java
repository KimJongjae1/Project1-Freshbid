package FreshBid.back.dto.live;

import FreshBid.back.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "판매자 정보 응답 DTO")
@Getter
@Setter
@NoArgsConstructor
public class SellerResponseDto {

    @Schema(description = "판매자 ID", example = "301")
    private Long sellerId;

    @Schema(description = "판매자 닉네임", example = "박봉팔")
    private String nickname;

    public static SellerResponseDto from(User user) {
        SellerResponseDto dto = new SellerResponseDto();
        dto.sellerId = user.getId();
        dto.nickname = user.getNickname();
        return dto;
    }
}