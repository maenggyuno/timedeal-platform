package capstone.controller.seller;

import capstone.domain.Store;
import capstone.dto.seller.SellerStoreDto;
import capstone.dto.seller.SellerStoreInfoDto;
import capstone.repository.seller.SellerStoreCreateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/seller/store")
@RequiredArgsConstructor
public class SellerStoreQueryController {

    private final SellerStoreCreateRepository storeRepo;

    // 메인 대시보드에서 쓰는 목록 API: StoreDto로 반환(이 안에 name이 있어야 프론트가 표시됨)
    @GetMapping("/all")
    public ResponseEntity<List<SellerStoreDto>> getAllStores() {
        List<SellerStoreDto> list = storeRepo.findAll().stream()
            .map(this::toStoreDto)
            .toList();
        return ResponseEntity.ok(list);
    }

    // 나의 마트 상세
    @GetMapping("/{storeId}/info")
    public ResponseEntity<SellerStoreInfoDto> getStoreInfo(@PathVariable long storeId) {
        return storeRepo.findById(storeId)
            .map(this::toInfoDto)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ---- 내부 매핑 유틸 ----
    private SellerStoreDto toStoreDto(Store s) {
        SellerStoreDto dto = new SellerStoreDto();
        dto.setId(s.getStoreId());
        dto.setStoreName(s.getName());
        return dto;
    }

    private SellerStoreInfoDto toInfoDto(Store s) {
        SellerStoreInfoDto dto = new SellerStoreInfoDto();
        dto.setId(s.getStoreId());
        dto.setName(s.getName());
        dto.setAddress(s.getAddress());
        dto.setPhone(s.getPhoneNumber());
        dto.setOpenTime(s.getOpeningTime()==null? null : s.getOpeningTime().toString());
        dto.setCloseTime(s.getClosingTime()==null? null : s.getClosingTime().toString());
        dto.setLatitude(s.getLatitude()==null? null : s.getLatitude().doubleValue());
        dto.setLongitude(s.getLongitude()==null? null : s.getLongitude().doubleValue());
        return dto;
    }
}
