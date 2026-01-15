package capstone.dto.buyer.response;

public class BuyerOrderCancelInfoResponse {

    private Long productId;
    private int quantity;

    public BuyerOrderCancelInfoResponse(Long productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public Long getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }
}
