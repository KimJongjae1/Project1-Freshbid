package FreshBid.back.dto.product;

import lombok.Data;

@Data
public class CategorySearchRequestDto {
    private String name;         // 이름 검색용 (nullable)
    private Integer superId;     // 상위 카테고리 ID 검색용 (nullable)
}
