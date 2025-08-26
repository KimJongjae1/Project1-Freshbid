package FreshBid.back.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    //특정 bucket에 file을 올리는 method
    public String uploadImage(String prefix, MultipartFile file);

    public String getUrl(String filePath);

    public void deleteImage(String filePath);

    public String convertImageUrlToBlob(String url);
}
