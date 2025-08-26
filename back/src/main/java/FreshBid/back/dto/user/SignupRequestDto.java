package FreshBid.back.dto.user;

import FreshBid.back.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignupRequestDto {

    @NotBlank(message = "아이디는 필수입니다")
    @Size(min = 8, max = 20, message = "아이디는 8자 이상 20자 이하로 입력하세요.")
    private String username; // 아이디

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Pattern(regexp = "^[A-Za-z0-9]{8,20}$", message = "비밀번호는 8~20자 사이의 영문자, 숫자를 포함해야 합니다.")
    private String password;

    @NotBlank(message = "닉네임은 필수입니다.")
    private String nickname;

    private User.Role role = User.Role.ROLE_CUSTOMER;
    @Email(message = "이메일 형식에 맞지 않습니다.")
    private String email;

    // 여기부터 optional
    private String phoneNumber;
    private String accountNumber;
    private String address;
}
