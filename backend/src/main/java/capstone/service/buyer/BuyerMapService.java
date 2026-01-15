package capstone.service.buyer;

import capstone.domain.Store;
import capstone.dto.buyer.response.BuyerStoreLocationResponse;
import capstone.dto.buyer.response.BuyerStoreMapResponse;
import capstone.repository.buyer.BuyerMapRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BuyerMapService {

    private final BuyerMapRepository mapRepository;

    public BuyerMapService(BuyerMapRepository mapRepository) {
        this.mapRepository = mapRepository;
    }

    // 모든 상점 검색 (사용하지는 않음)
    public List<BuyerStoreMapResponse> getAllStores() {
        // 1. DB에서 조회
        List<Store> stores = mapRepository.findAll();

        // 2. List로 받은 걸 DTO 리스트로 변환
        List<BuyerStoreMapResponse> response = new ArrayList<>();
        for (Store store : stores) {
            BuyerStoreMapResponse dto = new BuyerStoreMapResponse(
                    store.getStoreId(), store.getName(), store.getAddress(),
                    store.getLatitude(), store.getLongitude(), store.getPhoneNumber(),
                    store.getOpeningTime(), store.getClosingTime()
            );
            response.add(dto);
        }
        return response;
    }

    // 근처 상점 검색
    public List<BuyerStoreMapResponse> getNearbyStores(double latitude, double longitude) {
        // 1. DB에서 조회
        double distance = 5.0;
        List<Store> stores = mapRepository.findNearbyStores(latitude, longitude, distance);

        // 2. List로 받은 걸 DTO 리스트로 변환
        List<BuyerStoreMapResponse> response = new ArrayList<>();
        for (Store store : stores) {
            BuyerStoreMapResponse dto = new BuyerStoreMapResponse(
                    store.getStoreId(), store.getName(), store.getAddress(),
                    store.getLatitude(), store.getLongitude(), store.getPhoneNumber(),
                    store.getOpeningTime(), store.getClosingTime()
            );
            response.add(dto);
        }
        return response;
    }

    public BuyerStoreLocationResponse getStoreLocation(Long storeId) {
        return mapRepository.getStoreLocation(storeId);
    }
}
