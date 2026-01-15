package capstone.domain;

// 8월 9일 - 맹균오 생성
// 전체교체: 더 이상 enum 아님. 한글 카테고리(소분류) 상수/그룹 모음
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DB에는 소분류(한글) 그대로 저장한다.
 * 이 클래스는 프론트와 백에서 사용할 수 있는 카테고리 정의를 모아 둔 유틸성 컨테이너다.
 */
public final class Category {
    private Category() {}

    // 🔸 대분류 → 소분류 목록
    public static final Map<String, List<String>> GROUPS = Map.ofEntries(
            Map.entry("신선식품", List.of("과일", "채소", "육류", "수산물", "유제품")),
            Map.entry("가공식품", List.of("통조림/병조림", "즉석식품", "소스/양념", "간편조리식")),
            Map.entry("건강/특수식품", List.of("유기농/친환경", "비건/채식", "다이어트/저칼로리")),
            Map.entry("베이커리/디저트", List.of("빵", "케이크/파이", "쿠키/스낵")),
            Map.entry("음료", List.of("생수/탄산수", "주스/스무디", "커피/차", "기능성 음료"))
    );

    // 🔸 모든 소분류(검증용)
    public static final List<String> ALL =
            GROUPS.values().stream().flatMap(List::stream).collect(Collectors.toList());
}

