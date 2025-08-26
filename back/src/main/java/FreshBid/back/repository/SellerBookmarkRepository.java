package FreshBid.back.repository;

import FreshBid.back.entity.SellerBookmark;
import FreshBid.back.entity.SellerBookmarkKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SellerBookmarkRepository extends JpaRepository<SellerBookmark, SellerBookmarkKey> {
}
