package FreshBid.back.dto.price;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceDataRequestDto {
    
    private Long categoryId;
    private String categoryName;
    private Long superCategoryId;
    private String superCategoryName;
    private String grade;
}
