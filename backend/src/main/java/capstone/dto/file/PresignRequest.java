package capstone.dto.file;

//8월 9일 균오 생성
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class PresignRequest {
    private String filename;     // 예: myphoto.png
    private String contentType;  // 예: image/png
}
