package FreshBid.back.dto.live;

import FreshBid.back.entity.Live;
import lombok.AllArgsConstructor;
import lombok.Data;

//특정 판매자 페이지에 들어갔을 때 경매 filter
@Data
@AllArgsConstructor
public class SellerLiveFilterRequestDto {

    private Long sellerId;

    private Integer pageNo;

    private Integer pageSize;

    private String title;

    private Boolean isEnded;
}
