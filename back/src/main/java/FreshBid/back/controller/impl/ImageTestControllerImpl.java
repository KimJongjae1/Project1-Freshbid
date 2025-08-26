package FreshBid.back.controller.impl;

import FreshBid.back.controller.ImageTestController;
import FreshBid.back.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
public class ImageTestControllerImpl implements ImageTestController {
    private final FileStorageService imageService;
    @Override
    public ResponseEntity<String> uploadImage(MultipartFile file) {
        String bucketName = "test";
        return ResponseEntity.ok(imageService.uploadImage(bucketName, file));
    }
}
