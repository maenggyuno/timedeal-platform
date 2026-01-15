package capstone.dto.buyer.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ReviewResponse {

    private Integer rating;
    private String content;
    private LocalDateTime createdAt;
}
