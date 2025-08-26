package FreshBid.back.entity;

import FreshBid.back.dto.user.SignupRequestDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "`user`") // user는 예약어 가능성이 있으므로 백틱 사용
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false, length = 100)
    private String nickname;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(name = "profile_image", length = 100)
    private String profileImage;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(columnDefinition = "TEXT")
    private String introduction;

    @Column(name = "account_number", length = 100)
    private String accountNumber;

    @Column(length = 100)
    private String address;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum Role {
        ROLE_ADMIN,
        ROLE_CUSTOMER,
        ROLE_SELLER
    }

    //다음과 같은 static method를 통해 객체를 생성한다. AllArgsConstructor자제
    public static User of(SignupRequestDto signupRequestDto) {
        User user = new User();

        user.username = signupRequestDto.getUsername();
        user.password = signupRequestDto.getPassword();
        user.nickname = signupRequestDto.getNickname();
        user.phoneNumber = signupRequestDto.getPhoneNumber();
        user.email = signupRequestDto.getEmail();
        user.role = signupRequestDto.getRole();
        user.address = signupRequestDto.getAddress();
        user.accountNumber = signupRequestDto.getAccountNumber();

        return user;
    }
}
