package capstone.service.seller.sgis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SgisService {

    private final ObjectMapper objectMapper;

    @Value("${api.sgis.base-url}")
    private String baseUrl;

    @Value("${api.sgis.consumer-key}")
    private String consumerKey;

    @Value("${api.sgis.consumer-secret}")
    private String consumerSecret;

    @Value("${frontend.url}")
    private String frontendUrl;

    // 토큰 캐싱을 위한 변수 (메모리에 저장)
    private String cachedAccessToken;
    private long tokenExpirationTime = 0; // 토큰 만료 시간 (Timestamp)

    /**
     * 주소를 입력받아 위경도(WGS84) 정보를 반환합니다.
     * (내부적으로 토큰이 유효한지 확인하고 자동으로 갱신합니다)
     */
    public Map<String, Object> getGeocode(String address) {
        // 1. 유효한 토큰 가져오기 (없으면 새로 발급)
        String token = getValidAccessToken();

        // 2. Geocoding API 호출 URL 생성
        // 예: /OpenAPI3/addr/geocodewgs84.json?accessToken=...&address=...
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/OpenAPI3/addr/geocodewgs84.json")
                .queryParam("accessToken", token)
                .queryParam("address", address)
                .build()
                .encode() // 한글 주소 인코딩 처리
                .toUri();

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .header("Referer", frontendUrl)
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // JSON 파싱 후 결과 반환
            Map<String, Object> responseMap = objectMapper.readValue(response.body(), Map.class);

            // (선택) 만약 토큰 만료 에러(-401)가 뜨면 여기서 토큰 초기화 후 재시도 로직 추가 가능
            return responseMap;

        } catch (Exception e) {
            log.error("SGIS Geocoding Error", e);
            return Map.of("errCd", -1, "errMsg", "Backend API Error");
        }
    }

    /**
     * [내부 메서드] 토큰 관리 (캐싱 로직 포함)
     * - 토큰이 없거나 만료 시간이 지났으면 새로 발급받음
     * - 유효하면 기존 토큰 반환
     */
    private synchronized String getValidAccessToken() {
        long now = System.currentTimeMillis();

        // 토큰이 있고, 만료 시간보다 5분(300000ms) 정도 여유가 있다면 재사용
        if (cachedAccessToken != null && now < tokenExpirationTime - 300000) {
            return cachedAccessToken;
        }

        log.info("SGIS 토큰이 만료되었거나 없습니다. 새로 발급받습니다.");
        fetchNewAccessToken();
        return cachedAccessToken;
    }

    /**
     * [내부 메서드] 실제 SGIS 인증 API 호출
     */
    private void fetchNewAccessToken() {
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                    .path("/OpenAPI3/auth/authentication.json")
                    .queryParam("consumer_key", consumerKey)
                    .queryParam("consumer_secret", consumerSecret)
                    .build()
                    .toUri();

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().header("Referer", frontendUrl).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("SGIS Response: " + response.body()); // 테스트용 디버깅

            // 응답 파싱
            JsonNode rootNode = objectMapper.readTree(response.body());
            JsonNode resultNode = rootNode.path("result");

            if (!resultNode.isMissingNode() && resultNode.has("accessToken")) {
                this.cachedAccessToken = resultNode.get("accessToken").asText();
                // API가 주는 만료 시간은 보통 4시간. 여기서는 안전하게 현재시간 + 4시간 설정
                // 실제 응답의 'accessTimeout'을 파싱해서 써도 됨.
                this.tokenExpirationTime = System.currentTimeMillis() + (4 * 60 * 60 * 1000);
                log.info("SGIS 새 토큰 발급 완료: " + this.cachedAccessToken);
            } else {
                throw new RuntimeException("SGIS 토큰 발급 실패: " + response.body());
            }

        } catch (Exception e) {
            log.error("SGIS Token Fetch Error", e);
            throw new RuntimeException("SGIS 토큰을 가져오지 못했습니다.");
        }
    }
}
