package FreshBid.back.service.impl;

import FreshBid.back.dto.user.LoginRequestDto;
import FreshBid.back.dto.user.SignupRequestDto;
import FreshBid.back.entity.User;
import FreshBid.back.exception.DuplicateUserException;
import FreshBid.back.repository.AuthTokenRedisRepository;
import FreshBid.back.repository.UserRepository;
import FreshBid.back.repository.UserRepositorySupport;
import FreshBid.back.service.AuthService;
import FreshBid.back.service.FileStorageService;
import FreshBid.back.util.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtTokenProvider jwtTokenProvider;

    private final UserRepository userRepository;

    private final UserRepositorySupport userRepositorySupport;

    private final AuthTokenRedisRepository authTokenRedisRepository;

    private final PasswordEncoder passwordEncoder;

    private final FileStorageService fileStorageService;

    @Transactional
    @Override
    public Map<String, String> signup(SignupRequestDto signupRequestDto) {
        //아이디 또는 닉네임 중복 확인
        if (userRepositorySupport.findByUsername(signupRequestDto.getUsername()) != null) {
            throw new DuplicateUserException("사용중인 아이디입니다.");
        }
        if (userRepositorySupport.findByNickname(signupRequestDto.getNickname()) != null) {
            throw new DuplicateUserException("사용중인 닉네임입니다.");
        }
        //비밀번호 암호화 후 user 객체 생성
        String encodedPassword = passwordEncoder.encode(signupRequestDto.getPassword());
        signupRequestDto.setPassword(encodedPassword);
        User newUser = User.of(signupRequestDto);

        //신규 유저 jparepository에 저장
        userRepository.save(newUser);
        
        //jwt 토큰 발행
        String accessToken = jwtTokenProvider.generateAccessToken(newUser.getUsername());
        //refreshToken Redis에 저장
        String refreshToken = jwtTokenProvider.generateRefreshToken(newUser.getUsername());
        authTokenRedisRepository.saveToken(newUser.getUsername(), refreshToken);

        Map<String, String> tokens = new HashMap<String, String>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);

        return tokens;
    }

    @Transactional
    @Override
    public Map<String, String> login(LoginRequestDto loginRequestDto) {
        User user = userRepositorySupport.findByUsername(loginRequestDto.getUsername());
        if (user == null) {
            throw new UsernameNotFoundException("해당 아이디의 유저를 찾을 수 없습니다.");
        }
        //비밀번호 맞는지 확인
        if (passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
            //jwt 토큰 발행
            String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername());
            //refreshToken Redis에 저장
            String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());
            authTokenRedisRepository.saveToken(user.getUsername(), refreshToken);

            Map<String, String> tokens = new HashMap<String, String>();
            tokens.put("accessToken", accessToken);
            tokens.put("refreshToken", refreshToken);

            return tokens;
//            LoginResponseDto loginResponse = LoginResponseDto.builder()
//                    .nickname(user.getNickname())
//                    .username(user.getUsername())
//                    .profileImage(fileStorageService.getUrl(user.getProfileImage()))
//                    .accessToken(accessToken)
//                    .refreshToken(refreshToken)
//                    .role(user.getRole()).build();
//
//            return loginResponse;
        } else {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }
    }
}
