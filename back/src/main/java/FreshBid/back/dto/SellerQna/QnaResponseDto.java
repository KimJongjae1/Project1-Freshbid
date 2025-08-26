package FreshBid.back.dto.SellerQna;

import FreshBid.back.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "판매자 문의 응답 DTO (답변과 함께 조회)")
@Data
@NoArgsConstructor
public class QnaResponseDto {

    private Long id;
    private WriterInfoDto writer;
    private String content;
    private Long parentId;
    private LocalDateTime createdAt;
    private List<QnaResponseDto> replies;

    @Getter
    @NoArgsConstructor
    public static class WriterInfoDto {
        private Long id;
        private String username;
        private String nickname;

        public static WriterInfoDto from(User writer) {
            WriterInfoDto dto = new WriterInfoDto();
            dto.id = writer.getId();
            dto.username = writer.getUsername();
            dto.nickname = writer.getNickname();
            return dto;
        }
    }
}
