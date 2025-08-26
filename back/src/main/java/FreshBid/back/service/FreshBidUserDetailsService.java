package FreshBid.back.service;

import FreshBid.back.dto.user.FreshBidUserDetails;
import FreshBid.back.entity.User;
import FreshBid.back.repository.UserRepositorySupport;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FreshBidUserDetailsService implements UserDetailsService {

    private final UserRepositorySupport userRepositorySupport;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepositorySupport.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("등록된 사용자가 없습니다.");
        }
        return new FreshBidUserDetails(user);
    }
}
