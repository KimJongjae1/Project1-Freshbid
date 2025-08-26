package FreshBid.back.dto.product;

import lombok.Data;

@Data
public class CategoryUpdateRequestDto {

    private String name;      // 수정은 선택적으로 가능
    private Integer superId;  // 상위 카테고리 변경 가능
}
