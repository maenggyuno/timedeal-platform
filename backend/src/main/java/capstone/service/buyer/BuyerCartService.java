package capstone.service.buyer;

import capstone.dto.buyer.response.BuyerCartItemResponse;
import capstone.repository.buyer.BuyerCartRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BuyerCartService {

    private final BuyerCartRepository cartRepository;

    public BuyerCartService(BuyerCartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public void addToCart(Long userId, Long productId, Long quantity) {
        cartRepository.addToCart(userId, productId, quantity);
    }

    public List<BuyerCartItemResponse> getCartItemsByUserId(Long userId) {
        return cartRepository.getCartItemsByUserId(userId);
    }

    public void deleteCartItems(Long userId, List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return;
        }
        cartRepository.deleteCartItems(userId, productIds);
    }
}
