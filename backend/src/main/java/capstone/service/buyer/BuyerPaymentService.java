package capstone.service.buyer;

import capstone.dto.buyer.request.BuyerCartItemRequest;
import capstone.dto.buyer.request.BuyerCartPaymentRequest;
import capstone.dto.buyer.request.TossPaymentConfirmRequest;
import capstone.repository.QrCodeRepository;
import capstone.repository.buyer.BuyerCartRepository;
import capstone.repository.buyer.BuyerPaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.*;


@Service
public class BuyerPaymentService {

    private final BuyerPaymentRepository paymentRepository;
    private final QrCodeRepository qrCodeRepository;
    private final BuyerCartRepository cartRepository;

    @Value("${toss.payments.key}")
    private String tossSecretKey;

    public BuyerPaymentService(BuyerPaymentRepository paymentRepository, QrCodeRepository qrCodeRepository, BuyerCartRepository cartRepository) {
        this.paymentRepository = paymentRepository;
        this.qrCodeRepository = qrCodeRepository;
        this.cartRepository = cartRepository;
    }

    /**
     *  현장 결제
     *  */
    @Transactional
    public Long onSitePayment(Long buyerId, Long productId, Integer quantity) {
        // 1. DB 연결 (orderItems에 추가) + orderId 추출
        Long orderId = paymentRepository.onSitePayment(buyerId, productId, quantity);

        // 2. QR 코드 생성
        createQrCodeForOrder(orderId, false);

        return orderId;
    }

    /**
     *  카드 결제
     * */
    @Transactional
    public Long confirmTossPayment(Long buyerId, TossPaymentConfirmRequest request) {
        // 1. 가격 검증
        validateCardPaymentAmount(request);

        // 2. 토스페이먼츠 API에 최종 승인 요청
        BuyerCartPaymentRequest cartRequest = new BuyerCartPaymentRequest(request.getPaymentKey(), request.getOrderId(), request.getAmount());
        confirmPaymentToToss(cartRequest);

        // 3. 검증 및 승인 완료 -> DB에 주문정보 저장
        Long orderId = paymentRepository.createCardPaymentOrder(buyerId, request);

        // 4. QR코드 생성
        createQrCodeForOrder(orderId, request.isReservation());

        return orderId;
    }


    /**
     *  장바구니 결제
     *  - 현장결제와 카드결제를 동시에 처리
     */
    @Transactional
    public List<Long> processCartPayment(Long buyerId, BuyerCartPaymentRequest request) {
        List<Long> createdOrderIds = new ArrayList<>();

        // 1. 결제 수단에 따라 상품 분류
        List<BuyerCartItemRequest> onSiteItems = new ArrayList<>();
        List<BuyerCartItemRequest> cardItems = new ArrayList<>();
        for (BuyerCartItemRequest item : request.getItems()) {
            if ("ON_SITE".equals(item.getPaymentMethod())) {
                onSiteItems.add(item);
            } else if ("CARD".equals(item.getPaymentMethod())) {
                cardItems.add(item);
            }
        }

        // 2. 현장결제 주문 처리
        for (BuyerCartItemRequest item : onSiteItems) {
            Long orderId = onSitePayment(buyerId, item.getProductId(), item.getQuantity());
            createdOrderIds.add(orderId);
        }

        // 3. 카드결제 주문 처리
        if (!cardItems.isEmpty()) {
            // 3-1. 가격 검증
            validateCardPaymentAmount(cardItems, request.getAmount());

            // 3-2. 결제 요청
            confirmPaymentToToss(request);

            // 3-3. 카드 결제 상품들 처리
            for (BuyerCartItemRequest item : cardItems) {
                long itemTotalPrice = calculateItemTotalPrice(item.getProductId(), item.getQuantity(), item.isReservation());
                TossPaymentConfirmRequest dbRequest = new TossPaymentConfirmRequest(item.getProductId(), item.getQuantity(), itemTotalPrice, item.isReservation());
                Long orderId = paymentRepository.createCardPaymentOrder(buyerId, dbRequest);
                createQrCodeForOrder(orderId, item.isReservation());
                createdOrderIds.add(orderId);
            }
        }

        // 4. 장바구니 삭제
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            List<Long> productIdsToClear = request.getItems().stream()
                    .map(BuyerCartItemRequest::getProductId)
                    .toList();

            cartRepository.deleteItemsFromCart(buyerId, productIdsToClear);
        }

        return createdOrderIds;
    }

    // 가격 계산 메서드
    private long calculateItemTotalPrice(Long productId, Integer quantity, boolean isReservation) {
        Long originalPrice = paymentRepository.findPriceByProductId(productId);
        double priceMultiplier = isReservation ? 1.10 : 1.0;
        return (long) Math.round(originalPrice * quantity * priceMultiplier);
    }

    // 가격 검증 메서드 - 장바구니 상품(여러 상품)
    private void validateCardPaymentAmount(List<BuyerCartItemRequest> cardItems, Long totalAmountFromClient) {
        long expectedAmount = cardItems.stream()
                .mapToLong(item -> calculateItemTotalPrice(item.getProductId(), item.getQuantity(), item.isReservation()))
                .sum();

        if (totalAmountFromClient.longValue() != expectedAmount) {
            throw new RuntimeException("결제 금액이 서버에서 계산된 금액과 일치하지 않습니다.");
        }
    }

    // 가격 검증 메서드 - 단일 상품
    private void validateCardPaymentAmount(TossPaymentConfirmRequest request) {
        long expectedAmount = calculateItemTotalPrice(request.getProductId(), request.getQuantity(), request.isReservation());
        if (request.getAmount().longValue() != expectedAmount) {
            throw new RuntimeException("결제 금액이 올바르지 않습니다. 위변조가 의심됩니다.");
        }
    }

    // 토스페이먼츠 결제 승인 요청 메서드
    private void confirmPaymentToToss(BuyerCartPaymentRequest request) {
        System.out.println("4. 백엔드가 최종 확인하는 orderId: " + request.getOrderId()); // 확인 4

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = createTossApiHeaders();
        Map<String, Object> body = new HashMap<>();
        body.put("orderId", request.getOrderId());
        body.put("amount", request.getAmount());
        body.put("paymentKey", request.getPaymentKey());

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity("https://api.tosspayments.com/v1/payments/confirm", requestEntity, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("토스페이먼츠 결제 승인에 실패했습니다.", e);
        }
    }

    // QR 코드 생성 메서드
    private void createQrCodeForOrder(Long orderId, boolean isReservation) {
        String uuid = UUID.randomUUID().toString();
        if (isReservation) {
            qrCodeRepository.createReservedQrCode(uuid, orderId);
        } else {
            qrCodeRepository.basicCreateQrCode(uuid, orderId);
        }
    }

    // 토스페이먼츠 API 인증 헤더 생성 메서드
    private HttpHeaders createTossApiHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String encodedAuth = Base64.getEncoder().encodeToString((tossSecretKey + ":").getBytes(StandardCharsets.UTF_8));
        headers.setBasicAuth(encodedAuth);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }
}
