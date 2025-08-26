package FreshBid.back.service.impl;

import FreshBid.back.dto.user.FreshBidUserDetails;
import FreshBid.back.service.FileStorageService;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucket;

    @Override
    public String uploadImage(String prefix, MultipartFile file) {
        String fileName = "";
        try {

            //1. 파일명 설정
            fileName = generateFileName(prefix, file.getOriginalFilename());
            log.info("이미지 저장. 파일 명: {}", bucket, fileName);

            //2. 이미지 업로드
            InputStream inputStream = file.getInputStream();
            String contentType = file.getContentType();
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(fileName)
                    .stream(inputStream, inputStream.available(), -1)
                    .contentType(contentType)
                    .build();
            minioClient.putObject(args);

        } catch (Exception e) {
            log.warn("Exception occurred while saving contents : {}", e.getMessage(), e);
        }
        return fileName;
    }

    @Override
    public String getUrl(String filePath) {
        if(filePath == null) return null;
        String url = null;
        try {
            log.info("filePath: {}", filePath);

            url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(filePath)
                            .expiry(12, TimeUnit.HOURS)
                            .build());

        } catch (Exception e) {
            log.warn("Exception Occurred while getting: {}", e.getMessage(), e);
        }
        return url;
    }

    @Override
    public void deleteImage(String filePath) {
        try{
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(filePath)
                    .build());
        } catch (Exception e) {
            log.warn("Exception Occured while removing: {}", e.getMessage(), e);
        }
    }

    //Minio에 저장되는 파일명 : profile-image/username_asdlkf-329skjlv-kncxm.png
    private String generateFileName(String prefix, String originalFileName) {
        //현재 인증 사용자 가져오기
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String username = "";
        if (principal instanceof FreshBidUserDetails userDetails) {
            username = userDetails.getUsername();
        } else if (principal instanceof String) {
            username = (String) principal;
        }

        //확장자 가져오기
        String ext = "";
        int dotIndex = originalFileName.lastIndexOf(".");
        if (dotIndex > 0) {
            ext = originalFileName.substring(dotIndex); // .png, .jpg 등 확장자 추출
        }

        return prefix + "/"+ username + "_" + UUID.randomUUID() + ext;
    }

    @Override
    public String convertImageUrlToBlob(String url)  {
        if(url == null) return null;
        log.info("current image url: {}", url);
        RestTemplate restTemplate = new RestTemplate();
        try{
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    new URI(url),
                    HttpMethod.GET,
                    null,
                    byte[].class
            );
            return Base64.getEncoder().encodeToString(response.getBody());
        } catch (URISyntaxException e) {
            log.warn("URI syntax exception: {}", e.getMessage());
        }
        return null;
    }
}
