package capstone.service;

import capstone.domain.QrCode;
import capstone.dto.qrcode.QrCodeProductResponse;
import capstone.dto.qrcode.QrCodeRequest;
import capstone.repository.QrCodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class QrCodeService {

    private final QrCodeRepository qrCodeRepository;

    public QrCodeService(QrCodeRepository qrCodeRepository) {
        this.qrCodeRepository = qrCodeRepository;
    }

    /**
     *  QR 코드 조회
     * */
    @Transactional(readOnly = true)
    public QrCodeRequest getValidQrCodeByOrderId(Long orderId) {
        return qrCodeRepository.findByOrderIdAndValidUntilAfter(orderId, LocalDateTime.now())
                .map(this::convertToDto)
                .orElseThrow(() -> new RuntimeException("유효한 QR 코드를 찾을 수 없습니다. (orderId: " + orderId + ")"));
    }

    // qr코드 내용 -> DTO로 변환
    private QrCodeRequest convertToDto(QrCode qrCode) {
        QrCodeRequest dto = new QrCodeRequest();
        dto.setUuid(qrCode.getUuid());
        dto.setValidUntil(qrCode.getValidUntil());
        dto.setOrderId(qrCode.getOrderId());
        return dto;
    }

    /**
     *  QR코드 상품조회
     * */
    public QrCodeProductResponse getProductFromQrCode(Long orderId) {
        return qrCodeRepository.findProductFromQrCode(orderId);
    }
}
