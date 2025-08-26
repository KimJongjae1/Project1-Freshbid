package FreshBid.back.service;

import FreshBid.back.dto.user.LoginRequestDto;
import FreshBid.back.dto.user.SignupRequestDto;

import java.util.Map;

public interface AuthService {

    public Map<String, String> signup(SignupRequestDto signupRequestDto);

    public Map<String, String> login(LoginRequestDto loginRequestDto);
}
