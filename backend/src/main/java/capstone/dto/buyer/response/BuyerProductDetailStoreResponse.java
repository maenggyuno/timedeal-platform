package capstone.dto.buyer.response;

import lombok.Getter;

@Getter
public class BuyerProductDetailStoreResponse {

    private Long storeId;
    private String name;
    private String address;
    private Integer paymentMethod;
    private Double averageRating;
    private Long followersAmount;
    private Long reviewsAmount;
    private Boolean isFollowing;

    public BuyerProductDetailStoreResponse(Long storeId, String name, String address, Integer paymentMethod, Double averageRating, Long followersAmount, Long reviewsAmount, Boolean isFollowing) {
        this.storeId = storeId;
        this.name = name;
        this.address = address;
        this.paymentMethod = paymentMethod;
        this.averageRating = averageRating;
        this.followersAmount = followersAmount;
        this.reviewsAmount = reviewsAmount;
        this.isFollowing = isFollowing;
    }
}
