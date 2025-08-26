package FreshBid.back.validation;

import FreshBid.back.entity.Auction;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StatusListValidator implements
    ConstraintValidator<ValidStatusList, List<Auction.Status>> {

    private Set<Auction.Status> validStatuses;

    @Override
    public void initialize(ValidStatusList constraintAnnotation) {
        // Enum의 모든 값을 동적으로 가져와서 하드코딩 방지
        validStatuses = Arrays.stream(Auction.Status.values())
            .collect(Collectors.toSet());
    }

    @Override
    public boolean isValid(List<Auction.Status> statuses, ConstraintValidatorContext context) {
        if (statuses == null || statuses.isEmpty()) {
            return true; // null이나 빈 리스트는 유효하다고 간주
        }

        // 모든 status가 유효한 enum 값인지 확인
        return statuses.stream().allMatch(validStatuses::contains);
    }
}