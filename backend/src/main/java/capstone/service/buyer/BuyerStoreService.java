package capstone.service.buyer;

import capstone.repository.buyer.BuyerStoreRepository;
import org.springframework.stereotype.Service;

@Service
public class BuyerStoreService {

    private final BuyerStoreRepository storeRepository;

    public BuyerStoreService(BuyerStoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    public void followStore(Long userId, Long storeId) {
        storeRepository.followStore(userId, storeId);
    }

    public void unfollowStore(Long userId, Long storeId) {
        storeRepository.unfollowStore(userId, storeId);
    }
}
