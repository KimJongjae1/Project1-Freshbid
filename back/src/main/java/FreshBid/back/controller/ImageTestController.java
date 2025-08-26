package FreshBid.back.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/test-image")
public interface ImageTestController {

    //content-type: multipart/form-data
    @PostMapping
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file);
}
