package capstone.dto.seller;

import java.time.LocalDateTime;

public class SellerReviewDto {
    private Long reviewId;
    private String userName;
    private Integer stars;      // rating
    private String text;        // content
    private LocalDateTime createdAt;

    public SellerReviewDto(Long reviewId, String userName, Integer stars, String text, LocalDateTime createdAt) {
        this.reviewId = reviewId;
        this.userName = userName;
        this.stars = stars;
        this.text = text;
        this.createdAt = createdAt;
    }

    public Long getReviewId() { return reviewId; }
    public String getUserName() { return userName; }
    public Integer getStars() { return stars; }
    public String getText() { return text; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
