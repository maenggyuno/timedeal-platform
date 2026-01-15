package capstone.controller;

import capstone.dto.qrcode.QrCodeProductResponse;
import capstone.dto.qrcode.QrCodeRequest;
import capstone.service.QrCodeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/qrCode")
public class QrCodeController {

    private final QrCodeService qrCodeService;

    public QrCodeController(QrCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    /**
     *  QR코드 확인
     * */
    @GetMapping("/get")
    public ResponseEntity<QrCodeRequest> getQrCode(@RequestParam("orderId") Long orderId) {
        try {
            QrCodeRequest qrCodeDto = qrCodeService.getValidQrCodeByOrderId(orderId);
            return ResponseEntity.ok(qrCodeDto);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     *  QR코드로 상품조회
     * */
    @GetMapping("/product")
    public ResponseEntity<QrCodeProductResponse> getProductFromQrCode(@RequestParam("orderId") Long orderId) {
        QrCodeProductResponse response = qrCodeService.getProductFromQrCode(orderId);
        return ResponseEntity.ok(response);
    }
}
