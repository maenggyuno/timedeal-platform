package capstone.controller.file;

//8월 9일 균오 생성
import capstone.dto.file.PresignRequest;
import capstone.dto.file.PresignResponse;
import capstone.service.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;

    // 프리사인드 URL 발급
    @PostMapping("/presign")
    public ResponseEntity<PresignResponse> presign(@RequestBody PresignRequest req) {
        var result = s3Service.createPresignedPutUrl(req.getFilename(), req.getContentType());
        return ResponseEntity.ok(new PresignResponse(result.url(), result.key(), result.publicUrl()));
    }
}

