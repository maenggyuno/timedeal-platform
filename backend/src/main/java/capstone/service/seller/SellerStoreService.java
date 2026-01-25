package capstone.service.seller;

import capstone.domain.Store;
import capstone.domain.StoreEmployee;
import capstone.domain.StoreEmployeeId;
import capstone.dto.seller.SellerMyMartInfoResponse;
import capstone.dto.seller.request.SellerStoreCreateRequest;
import capstone.dto.seller.request.SellerStoreUpdateRequest;
import capstone.dto.seller.response.SellerDashboardResponse;
import capstone.dto.seller.response.SellerStoreInfoResponse;
import capstone.repository.seller.SellerStoreJdbcRepository;
import capstone.repository.seller.SellerStoreRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SellerStoreService {

    private final SellerStoreRepository sellerStoreRepository;
    private final SellerStoreJdbcRepository store_employeeRepository;

    @Transactional(readOnly = true)
    public List<SellerDashboardResponse> findAll(Long userId) {
        List<StoreEmployee> store_employees = store_employeeRepository.findById_UserId(userId);
        List<Long> storeIds = store_employees.stream()
                .map(se -> se.getId().getStoreId())
                .collect(Collectors.toList());
        if (storeIds.isEmpty()) {
            return List.of();
        }

        List<Store> stores = sellerStoreRepository.findAllByStoreIdInAndIsDeletedFalse(storeIds);
        Map<Long, Store> storeMap = stores.stream()
                .collect(Collectors.toMap(Store::getStoreId, store -> store));

        return store_employees.stream()
                .map(se -> {
                    Store store = storeMap.get(se.getId().getStoreId());
                    if (store == null) return null;
                    return new SellerDashboardResponse(
                            store.getStoreId(),
                            store.getName(),
                            store.getAddress(),
                            se.getAuthority()
                    );
                })
                .filter(response -> response != null)
                .collect(Collectors.toList());
    }

    @Transactional
    public Store create(Long userId, SellerStoreCreateRequest request) {
        Store storeToSave = request.toEntity(userId);
        Store createdStore = sellerStoreRepository.save(storeToSave);

        StoreEmployeeId store_employeeId = new StoreEmployeeId(createdStore.getStoreId(), userId);
        StoreEmployee ownerAsEmployee = StoreEmployee.builder()
                .id(store_employeeId)
                .authority(1)
                .build();

        store_employeeRepository.save(ownerAsEmployee);

        return createdStore;
    }

    public SellerMyMartInfoResponse getMyMartInfo(Long storeId) {
        // findMyMartInfo 메서드를 호출하고, 결과가 없으면 예외를 발생시킵니다.
        return sellerStoreRepository.findMyMartInfo(storeId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 가게를 찾을 수 없습니다: " + storeId));
    }

    @Transactional(readOnly = true)
    public SellerStoreInfoResponse getStoreInfo(Long storeId) {
        return sellerStoreRepository.findByStoreIdAndIsDeletedFalse(storeId)
                .map(SellerStoreInfoResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("활성화된 매장을 찾을 수 없습니다. ID: " + storeId));
    }

    @Transactional
    public void updateName(Long storeId, SellerStoreUpdateRequest.Name request) {
        Store store = sellerStoreRepository.findByStoreIdAndIsDeletedFalse(storeId)
                .orElseThrow(() -> new IllegalArgumentException("활성화된 매장을 찾을 수 없습니다."));
        store.setName(request.getName());
        sellerStoreRepository.save(store);
    }

    @Transactional
    public void updateAddress(Long storeId, SellerStoreUpdateRequest.Address request) {
        Store store = sellerStoreRepository.findByStoreIdAndIsDeletedFalse(storeId)
                .orElseThrow(() -> new IllegalArgumentException("활성화된 매장을 찾을 수 없습니다."));
        store.setAddress(request.getAddress());
        store.setLatitude(request.getLatitude());
        store.setLongitude(request.getLongitude());
        sellerStoreRepository.save(store);
    }

    @Transactional
    public void updatePhoneNumber(Long storeId, SellerStoreUpdateRequest.PhoneNumber request) {
        Store store = sellerStoreRepository.findByStoreIdAndIsDeletedFalse(storeId)
                .orElseThrow(() -> new IllegalArgumentException("활성화된 매장을 찾을 수 없습니다."));
        store.setPhoneNumber(request.getPhoneNumber());
        sellerStoreRepository.save(store);
    }

    @Transactional
    public void updateHours(Long storeId, SellerStoreUpdateRequest.OperatingHours request) {
        Store store = sellerStoreRepository.findByStoreIdAndIsDeletedFalse(storeId)
                .orElseThrow(() -> new IllegalArgumentException("활성화된 매장을 찾을 수 없습니다."));
        store.setOpeningTime(request.getOpeningTime());
        store.setClosingTime(request.getClosingTime());
        sellerStoreRepository.save(store);
    }

    @Transactional
    public void updatePaymentMethod(Long storeId, SellerStoreUpdateRequest.PaymentMethod request) {
        Store store = sellerStoreRepository.findByStoreIdAndIsDeletedFalse(storeId)
                .orElseThrow(() -> new IllegalArgumentException("활성화된 매장을 찾을 수 없습니다."));
        store.setPaymentMethod(request.getPaymentMethod());
        sellerStoreRepository.save(store);
    }

    @Transactional
    public void deleteStore(Long storeId) {
        Store store = sellerStoreRepository.findByStoreIdAndIsDeletedFalse(storeId)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 매장을 찾을 수 없거나 이미 삭제되었습니다."));

        store.softDelete();
        sellerStoreRepository.save(store);
    }
}
