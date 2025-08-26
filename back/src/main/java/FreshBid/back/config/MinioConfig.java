package FreshBid.back.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@Getter
@Slf4j
public class MinioConfig {
    @Value("${minio.endpoint}")
    private String url;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String accessSecret;

    @Value("${minio.bucket-name}")
    private String bucket;

    @Bean
    public MinioClient minioClient() throws Exception{
        MinioClient minioClient = MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, accessSecret)
                .build();

        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
        } catch (Exception e) {
            log.warn("bucketError found : {}", e.getMessage(), e);
        }
        return minioClient;
    }

}
