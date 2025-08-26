package FreshBid.back.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Data
public class LiveBookmarkKey implements Serializable {

    @Column(name = "live_id")
    private Long liveId;

    @Column(name = "user_id")
    private Long userId;
}
