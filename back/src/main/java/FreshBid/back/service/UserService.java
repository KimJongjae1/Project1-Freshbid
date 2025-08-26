package FreshBid.back.service;

import FreshBid.back.dto.user.UserBasicResponseDto;
import org.springframework.web.multipart.MultipartFile;

import FreshBid.back.dto.user.UserInfoUpdateRequestDto;

public interface UserService {

	public void updateUserInfo(Long userId, UserInfoUpdateRequestDto dto);

	public UserBasicResponseDto getUserBasicInfo(Long userId);
}
