package capstone.service.s3;

//8월 9일 균오 생성

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

//    private final S3Client s3Client;
    private final S3Presigner presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.region}")
    private String region;

    // 프리사인드 URL 발급
    public PresignResult createPresignedPutUrl(String filename, String contentType) {
        String key = "products/%s_%s".formatted(UUID.randomUUID(), filename);

        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .putObjectRequest(put)
                .signatureDuration(Duration.ofMinutes(10)) // ✏️ 변경 가능
                .build();

        PresignedPutObjectRequest presigned = presigner.presignPutObject(presignRequest);
        URL url = presigned.url();

        String publicUrl = "https://%s.s3.%s.amazonaws.com/%s".formatted(bucket, region, key);

        return new PresignResult(url.toString(), key, publicUrl);
    }

    // 내부 반환용
    public record PresignResult(String url, String key, String publicUrl) {}
}

