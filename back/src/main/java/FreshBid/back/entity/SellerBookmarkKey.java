package FreshBid.back.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerBookmarkKey implements Serializable {

    @Column(name = "seller_id")
    private Long sellerId;
    @Column(name = "user_id")
    private Long userId;
}
