package capstone.service.buyer;

import capstone.dto.buyer.response.BuyerProductDetailResponse;
import capstone.dto.buyer.response.BuyerProductSearchResponse;
import capstone.repository.buyer.BuyerProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BuyerProductService {

    private final BuyerProductRepository productRepository;

    public BuyerProductService(BuyerProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<BuyerProductSearchResponse> getProductsNearby(double latitude, double longitude, double distance, int page, int size) {
        return productRepository.getProductsNearby(latitude, longitude, distance, page, size);
    }

    public BuyerProductDetailResponse getProductById(Long userId, Long productId) {
        return productRepository.getProductById(userId, productId);
    }

    public List<BuyerProductSearchResponse> getNearbyProductsByCategory(double latitude, double longitude, double distance, String category, int page, int size) {
        return productRepository.getNearbyProductsByCategory(latitude, longitude, distance, category, page, size);
    }

    public List<BuyerProductSearchResponse> getNearbyProductsBySearchString(double latitude, double longitude, double distance, String searchString, int page, int size) {
        return productRepository.getNearbyProductsBySearchString(latitude, longitude, distance, searchString, page, size);
    }

    public List<BuyerProductSearchResponse> getProductsByStore(Long storeId, int page, int size) {
        return productRepository.getProductsByStore(storeId, page, size);
    }
}
