package FreshBid.back.dto.SellerQna;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "판매자 문의 등록 요청 DTO")
@Getter
@Setter
@NoArgsConstructor
public class SellerQnaCreateRequestDto {

    @NotNull
    @Schema(description = "판매자 ID", example = "3")
    private Long sellerId;

    @Schema(description = "부모 문의 ID (답변인 경우)", example = "1")
    private Long superId;

    @NotBlank
    @Schema(description = "문의 내용", example = "상품에 대해 문의드립니다.")
    private String content;
} 