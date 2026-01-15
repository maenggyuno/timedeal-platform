//package capstone.dto.buyer.response;
//
//import lombok.Getter;
//
//@Getter
//public class BuyerProductSearchResponse {
//
//    private Long productId;
//    private String imageUrl;
//    private String name;
//    private Long price;
//    private String category;    // 테스트를 위해 임시 추가
//
//    public BuyerProductSearchResponse(Long productId, String imageUrl, String name, Long price, String category) {
//        this.productId = productId;
//        this.imageUrl = imageUrl;
//        this.name = name;
//        this.price = price;
//        this.category = category;
//    }
//}


package capstone.dto.buyer.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BuyerProductSearchResponse {

    private Long productId;
    private String imageUrl;
    private String name;
    private Long price;
    private String category;
    private Boolean isStoreDeleted;

    public BuyerProductSearchResponse(Long productId, String imageUrl, String name, Long price, String category) {
        this(productId, imageUrl, name, price, category, null);
    }

    public BuyerProductSearchResponse(Long productId, String imageUrl, String name, Long price, String category, Boolean isStoreDeleted) {
        this.productId = productId;
        this.imageUrl = imageUrl;
        this.name = name;
        this.price = price;
        this.category = category;
        this.isStoreDeleted = isStoreDeleted;
    }
}