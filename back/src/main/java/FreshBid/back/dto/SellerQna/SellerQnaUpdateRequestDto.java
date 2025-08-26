package FreshBid.back.dto.SellerQna;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "판매자 문의 수정 요청 DTO")
@Getter
@Setter
@NoArgsConstructor
public class SellerQnaUpdateRequestDto {

    @NotBlank
    @Schema(description = "문의 내용", example = "수정된 문의 내용입니다.")
    private String content;
} 