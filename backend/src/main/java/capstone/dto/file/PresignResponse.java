package capstone.dto.file;

//8월 9일 균오 생성

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PresignResponse {
    private String url;        // 프리사인드 PUT URL
    private String key;        // s3 객체 키 (products/uuid_filename)
    private String publicUrl;  // 공개 URL (버킷이 공개라면 즉시 사용 가능)
}
