package FreshBid.back.dto.product;

import FreshBid.back.entity.ProductCategory;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryResponseDto {

    private Integer id;
    private String name;
    private Integer superId;

    public static CategoryResponseDto fromEntity(ProductCategory entity) {
        return CategoryResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .superId(entity.getSuperCategory() != null ? entity.getSuperCategory().getId().intValue() : null)
                .build();
    }
}
