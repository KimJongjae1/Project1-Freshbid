package FreshBid.back.service.impl;

import FreshBid.back.dto.user.UserBasicResponseDto;
import FreshBid.back.dto.user.UserInfoUpdateRequestDto;
import FreshBid.back.entity.User;
import FreshBid.back.exception.DuplicateUserException;
import FreshBid.back.exception.UserNotFoundException;
import FreshBid.back.repository.UserRepository;
import FreshBid.back.repository.UserRepositorySupport;
import FreshBid.back.service.FileStorageService;
import FreshBid.back.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final UserRepositorySupport userRepositorySupport;
	private final FreshBid.back.util.JwtTokenProvider jwtTokenProvider;
	public static final String MINIO_PREFIX = "profile-image";
	private final FileStorageService fileStorageService;
	@Transactional
	public void updateUserInfo(Long userId, UserInfoUpdateRequestDto dto) {

		User user = userRepositorySupport.findById(userId);
		if (user == null) {
			throw new UserNotFoundException(userId);
		}

		if (dto.getNickname() != null && !dto.getNickname().equals(user.getNickname())) {
			if (userRepositorySupport.findByNickname(dto.getNickname()) != null) {
				throw new DuplicateUserException("이미 사용 중인 닉네임입니다.");
			}
			user.setNickname(dto.getNickname());
		}

		 if (dto.getProfileImageFile() != null && !dto.getProfileImageFile().isEmpty()) {
			 //기존 프로필 이미지가 있다면 삭제
			 if(user.getProfileImage() != null) {
				 log.info("erase prev file: {}", user.getProfileImage());
				 fileStorageService.deleteImage(user.getProfileImage());
			 }

			 //새 프로필 업로드 후 저장
			 String fileName = fileStorageService.uploadImage(MINIO_PREFIX, dto.getProfileImageFile());
			 log.info("newFileName: {}", fileName);
			 user.setProfileImage(fileName);
		 }

		if (dto.getPhoneNumber() != null) {
			user.setPhoneNumber(dto.getPhoneNumber());
		}
		if (dto.getAddress() != null) {
			user.setAddress(dto.getAddress());
		}
		if (dto.getEmail() != null) {
			user.setEmail(dto.getEmail());
		}
		if (dto.getIntroduction() != null)
			user.setIntroduction(dto.getIntroduction());

		userRepository.save(user);
	}

	@Override
	public UserBasicResponseDto getUserBasicInfo(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UserNotFoundException(userId));

		String profileImageUrl = fileStorageService.getUrl(user.getProfileImage());

        return UserBasicResponseDto.builder()
				.username(user.getUsername())
				.nickname(user.getNickname())
				.role(user.getRole())
				.profileImage(fileStorageService.convertImageUrlToBlob(profileImageUrl))
				.build();
	}
}
