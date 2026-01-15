package capstone.repository.seller;

import capstone.domain.Store;
import capstone.dto.seller.SellerMyMartInfoResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SellerStoreRepository extends JpaRepository<Store, Long> {

    List<Store> findAllByStoreIdIn(List<Long> storeIds);

    // isDeleted가 false인 가게만 조회하도록 조건 추가
    List<Store> findAllByStoreIdInAndIsDeletedFalse(List<Long> storeIds);

    // ID로 조회 시에도 isDeleted가 false인 가게만 찾도록 메서드 추가
    Optional<Store> findByStoreIdAndIsDeletedFalse(Long storeId); // 실제 필드명인 StoreId 사용

    @Query("SELECT new capstone.dto.seller.SellerMyMartInfoResponse(" +
            "s.storeId, s.name, s.address, s.phoneNumber, " +
            "CAST(s.openingTime AS string), CAST(s.closingTime AS string), " +
            "s.latitude, s.longitude) " +
            "FROM Store s " +
            "WHERE s.storeId = :storeId AND s.isDeleted = false")
    Optional<SellerMyMartInfoResponse> findMyMartInfo(@Param("storeId") Long storeId);
}