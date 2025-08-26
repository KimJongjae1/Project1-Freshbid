package FreshBid.back.repository;

import FreshBid.back.entity.LiveBookmark;
import FreshBid.back.entity.LiveBookmarkKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LiveBookmarkRepository extends JpaRepository<LiveBookmark, LiveBookmarkKey> {
}
