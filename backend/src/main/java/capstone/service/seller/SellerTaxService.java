package capstone.service.seller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SellerTaxService {

    // Java 객체를 JSON 문자열로 변환하기 위한 도구
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${api.gonggong.serviceKey}")
    private String serviceKey;

    private static final String API_URL = "https://api.odcloud.kr/api/nts-businessman/v1/status?serviceKey=";

    /**
     * 공공데이터 API를 호출하여 사업자 번호 상태를 조회합니다.
     * @param businessNumber 조회할 사업자 번호
     * @return API 응답 결과
     */
    public Map<String, Object> checkBusinessNumberStatus(String businessNumber) {
        String fullUrl = API_URL + serviceKey;

        // API 요청 본문(Body) 데이터 생성
        Map<String, Object> requestBodyMap = new HashMap<>();
        requestBodyMap.put("b_no", Collections.singletonList(businessNumber));

        try {
            // 1. 요청 본문을 JSON 문자열로 변환
            String requestBodyJson = objectMapper.writeValueAsString(requestBodyMap);

            // 2. Java 11의 기본 HttpClient 생성
            HttpClient client = HttpClient.newHttpClient();

            // 3. HTTP 요청 객체 생성
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(fullUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .build();

            // 4. 요청을 보내고 응답을 문자열(String) 형태로 받음
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // 5. 응답받은 JSON 문자열을 Map 형태로 변환하여 반환
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = objectMapper.readValue(response.body(), Map.class);
            return responseMap;

        } catch (Exception e) {
            // API 호출 실패 시 예외 처리
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status_code", "ERROR");
            errorResponse.put("match_cnt", 0);
            errorResponse.put("data", Collections.emptyList());
            return errorResponse;
        }
    }
}
