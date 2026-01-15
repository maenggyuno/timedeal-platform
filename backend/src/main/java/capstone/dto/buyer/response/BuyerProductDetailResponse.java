package capstone.dto.buyer.response;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BuyerProductDetailResponse {

    private Long productId;
    private String imageUrl;
    private String name;
    private Long price;
    private Integer quantity;
    private LocalDateTime expirationDate;
    private String description;
    private BuyerProductDetailStoreResponse store;
    private String category;

    public BuyerProductDetailResponse(Long productId, String imageUrl, String name, Long price, Integer quantity, LocalDateTime expirationDate, String description, BuyerProductDetailStoreResponse store, String category) {
        this.productId = productId;
        this.imageUrl = imageUrl;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.expirationDate = expirationDate;
        this.description = description;
        this.store = store;
        this.category = category;
    }
}
