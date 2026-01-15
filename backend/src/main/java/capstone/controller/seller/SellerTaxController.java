package capstone.controller.seller;

import capstone.service.seller.SellerTaxService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/seller/tax")
@RequiredArgsConstructor
public class SellerTaxController {

    private final SellerTaxService taxService;

    /**
     * 사업자등록번호 상태 조회
     * @param bno 사업자등록번호 10자리 (하이픈'-' 제외)
     * @return 국세청 API 조회 결과
     */
    @GetMapping("/biz-status")
    public ResponseEntity<Map<String, Object>> checkBusinessNumberStatus(@RequestParam("bno") String bno) {
        Map<String, Object> responseData = taxService.checkBusinessNumberStatus(bno);
        return ResponseEntity.ok(responseData);
    }
}