package capstone.service.s3;

//균오가 8월 9일에 생성
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3StorageService {

    private final S3Client s3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.region}")
    private String region;

    public String upload(MultipartFile file, String prefix) throws IOException {
        String ext = getExt(file.getOriginalFilename());
        String key = (prefix == null ? "" : prefix + "/") + UUID.randomUUID() + ext;

        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .acl(ObjectCannedACL.PUBLIC_READ) // URL로 접근 가능
                .build();

        s3.putObject(req, RequestBody.fromBytes(file.getBytes()));

        // 업로드된 파일 URL 리턴
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
    }

    private String getExt(String name) {
        if (name == null) return "";
        int idx = name.lastIndexOf('.');
        return idx > -1 ? name.substring(idx) : "";
    }
}
