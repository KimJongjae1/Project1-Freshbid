package FreshBid.back.controller.impl;

import FreshBid.back.controller.UtilController;
import FreshBid.back.dto.common.CommonResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/util")
public class UtilControllerImpl implements UtilController {

    @Override
    @GetMapping("/check-token")
    public ResponseEntity<?> checkToken() {
        CommonResponse<Void> response = CommonResponse.<Void>builder()
            .success(true)
            .message("유효한 토큰입니다.")
            .data(null)
            .build();
        return ResponseEntity.ok(response);
    }
}
