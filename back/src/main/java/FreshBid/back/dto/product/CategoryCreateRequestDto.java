package FreshBid.back.dto.product;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryCreateRequestDto {

    @NotBlank(message = "카테고리 이름은 필수입니다.")
    private String name;

    private Integer superId; // 상위 카테고리 ID는 선택적 (null 가능)
}
