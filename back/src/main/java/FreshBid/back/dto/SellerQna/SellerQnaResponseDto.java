package FreshBid.back.dto.SellerQna;

import FreshBid.back.entity.SellerQna;
import FreshBid.back.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Schema(description = "판매자 문의 응답 DTO")
@Getter
@NoArgsConstructor
public class SellerQnaResponseDto {

    @Schema(description = "문의 ID", example = "1")
    private Long id;

    @Schema(description = "판매자 정보")
    private SellerInfoDto seller;

    @Schema(description = "작성자 정보")
    private WriterInfoDto writer;

    @Schema(description = "부모 문의 ID", example = "1")
    private Long superId;

    @Schema(description = "문의 내용", example = "상품에 대해 문의드립니다.")
    private String content;

    @Schema(description = "생성일시")
    private LocalDateTime createdAt;

    @Schema(description = "답변 목록")
    private List<SellerQnaResponseDto> replies;

    public static SellerQnaResponseDto from(SellerQna qna) {
        SellerQnaResponseDto dto = new SellerQnaResponseDto();
        dto.id = qna.getId();
        dto.seller = SellerInfoDto.from(qna.getSeller());
        dto.writer = WriterInfoDto.from(qna.getWriter());
        dto.superId = qna.getSuperQna() != null ? qna.getSuperQna().getId() : null;
        dto.content = qna.getContent();
        dto.createdAt = qna.getCreatedAt();
        
        // 답변 목록 설정 (부모 문의인 경우에만)
        if (qna.getSuperQna() == null && qna.getReplies() != null) {
            dto.replies = qna.getReplies().stream()
                    .filter(reply -> !reply.isDeleted())
                    .map(SellerQnaResponseDto::from)
                    .collect(Collectors.toList());
        }
        
        return dto;
    }

    @Getter
    @NoArgsConstructor
    public static class SellerInfoDto {
        private Long id;
        private String nickname;
        private String profileImage;

        public static SellerInfoDto from(User seller) {
            SellerInfoDto dto = new SellerInfoDto();
            dto.id = seller.getId();
            dto.nickname = seller.getNickname();
            dto.profileImage = seller.getProfileImage();
            return dto;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class WriterInfoDto {
        private Long id;
        private String nickname;
        private String profileImage;

        public static WriterInfoDto from(User writer) {
            WriterInfoDto dto = new WriterInfoDto();
            dto.id = writer.getId();
            dto.nickname = writer.getNickname();
            dto.profileImage = writer.getProfileImage();
            return dto;
        }
    }
} 