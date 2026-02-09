////package capstone.config;
////
////import capstone.security.jwt.JwtAuthenticationFilter;
////import capstone.security.jwt.JwtTokenProvider;
////import capstone.security.oauth2.CustomOAuth2UserService;
////import capstone.security.oauth2.OAuth2LoginFailureHandler;
////import capstone.security.oauth2.OAuth2LoginSuccessHandler;
////import jakarta.servlet.http.HttpServletResponse;
////import org.springframework.context.annotation.Bean;
////import org.springframework.context.annotation.Configuration;
////import org.springframework.security.config.annotation.web.builders.HttpSecurity;
////import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
////import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
////import org.springframework.security.config.http.SessionCreationPolicy;
////import org.springframework.security.web.SecurityFilterChain;
////import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
////import org.springframework.web.cors.CorsConfiguration;
////import org.springframework.web.cors.CorsConfigurationSource;
////import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
////
////import java.util.Arrays;
////import java.util.List;
////
////@Configuration
////@EnableWebSecurity
////// @RequiredArgsConstructor // final 필드가 있으면 자동으로 생성자를 만들지만, 여기서는 명시적으로 생성자를 사용하지 않음 (필드가 주입됨)
////public class SecurityConfig {
////
////    private final CustomOAuth2UserService customOAuth2UserService;
////    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
////    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
////    private final JwtTokenProvider jwtTokenProvider; // final로 선언하고 생성자 주입 받도록 함
////
////    // 생성자 주입을 위해 추가
////    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService,
////                          OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler,
////                          OAuth2LoginFailureHandler oAuth2LoginFailureHandler,
////                          JwtTokenProvider jwtTokenProvider) {
////        this.customOAuth2UserService = customOAuth2UserService;
////        this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
////        this.oAuth2LoginFailureHandler = oAuth2LoginFailureHandler;
////        this.jwtTokenProvider = jwtTokenProvider;
////    }
////
////
////    @Bean
////    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
////        http
////                // CSRF 보호 비활성화 (JWT 사용 시 상태를 저장하지 않으므로)
////                .csrf(AbstractHttpConfigurer::disable)
////                // HTTP Basic 인증 비활성화
////                .httpBasic(AbstractHttpConfigurer::disable)
////                // Form 로그인 비활성화 (OAuth2 사용)
////                .formLogin(AbstractHttpConfigurer::disable)
////                // 세션 관리: STATELESS (JWT 기반이므로 세션 사용 안 함)
////                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
////                // CORS 설정
////                .cors(cors -> cors.configurationSource(corsConfigurationSource()));
////
////        http
////                // 요청 경로별 권한 설정
////                .authorizeHttpRequests(auth -> auth
////                        .requestMatchers(
////                                "/", "/error", "/favicon.ico",
////                                "/login/**", // OAuth2 로그인 프로세스 시작 URL (예: /login/oauth2/google)
////                                "/oauth2/**", // Spring Security OAuth2 내부 처리 경로 및 콜백 (예: /oauth2/code/google)
////                                // "/login/success", "/login/failure", // OAuth2RedirectHandler가 사용하는 경로 예시, 필요시 permitAll
////                                "/api/auth/health", // 인증 불필요한 API (예: 상태 체크)
////                                // React 빌드 파일 (static resources) - 정적 리소스를 Spring Boot가 서빙할 경우
////                                "/static/**", "/manifest.json", "/logo*.png", "/asset-manifest.json",
////                                "/index.html" // React 앱의 진입점
////                        ).permitAll()
////                        .requestMatchers("/api/admin/**").hasRole("ADMIN") // 예시: 관리자 권한
////                        .requestMatchers("/login/isJwtCookieExisted").authenticated() // 인증 상태 확인 API는 인증 필요
////                        .anyRequest().authenticated() // 그 외 모든 요청은 인증 필요
////                );
////
////        http
////                // OAuth2 로그인 설정
////                .oauth2Login(oauth2 -> oauth2
////                                .userInfoEndpoint(userInfo -> userInfo
////                                        .userService(customOAuth2UserService) // 커스텀 유저 서비스
////                                )
////                                .successHandler(oAuth2LoginSuccessHandler) // 로그인 성공 핸들러
////                                .failureHandler(oAuth2LoginFailureHandler) // 로그인 실패 핸들러
////                        // .loginPage("/login") // 프론트엔드의 로그인 페이지 (보통 /oauth2/authorization/{registrationId}로 직접 리다이렉션)
////                        // OAuth2 로그인 리다이렉션 URI 설정 (application.properties/yml에서도 설정 가능)
////                        // .redirectionEndpoint(redirection -> redirection
////                        // .baseUri("/login/oauth2/code/*")) // 예시: 콜백 기본 URI
////                );
////
////        http
////                // JWT 인증 필터 추가 (UsernamePasswordAuthenticationFilter 전에 실행)
////                // JwtAuthenticationFilter는 JwtTokenProvider에 의존하므로, JwtTokenProvider 빈을 주입받아 생성
////                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
////
////        // (선택 사항) 로그아웃 처리
////        http
////                .logout(logout -> logout
////                        .logoutUrl("/api/auth/logout") // 로그아웃 요청 URL
////                        .logoutSuccessHandler((request, response, authentication) -> {
////                            // 추가적인 로그아웃 성공 로직 (예: 로그 남기기)
////                            response.setStatus(HttpServletResponse.SC_OK);
////                        })
////                        // 여기서 jwtTokenProvider.getJwtCookieName()을 호출하여 정확한 쿠키 이름을 사용
////                        .deleteCookies("JSESSIONID", jwtTokenProvider.getJwtCookieName())
////                        .invalidateHttpSession(true) // STATELESS지만 명시적 처리
////                        .clearAuthentication(true) // 인증 정보 클리어
////                );
////
////        return http.build();
////    }
////
////    // CORS 설정 (React 개발 서버와 통신을 위해)
////    @Bean
////    public CorsConfigurationSource corsConfigurationSource() {
////        CorsConfiguration configuration = new CorsConfiguration();
////        // 로컬 개발 환경에서는 "http://localhost:3000"을 허용합니다.
////        // 실제 프로덕션 환경에서는 프론트엔드 배포 도메인을 추가해야 합니다.
////        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
////        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
////        configuration.setAllowedHeaders(List.of("*")); // 모든 헤더 허용 (실제 서비스에서는 필요한 헤더만 명시 권장)
////        configuration.setAllowCredentials(true); // 쿠키를 포함한 요청 허용
////        configuration.setMaxAge(3600L); // pre-flight 요청 캐시 시간 (초 단위)
////
////        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
////        source.registerCorsConfiguration("/**", configuration); // 모든 경로에 대해 이 CORS 설정 적용
////        return source;
////    }
////}
//
//package capstone.config;
//
//import capstone.security.jwt.JwtAuthenticationFilter;
//import capstone.security.jwt.JwtTokenProvider;
//import capstone.security.oauth2.CustomOAuth2UserService;
//import capstone.security.oauth2.OAuth2LoginFailureHandler;
//import capstone.security.oauth2.OAuth2LoginSuccessHandler;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.HttpStatus;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//
//import java.util.Arrays;
//import java.util.List;
//
//@Configuration
//@EnableWebSecurity
//@RequiredArgsConstructor
//public class SecurityConfig {
//
//    private final CustomOAuth2UserService customOAuth2UserService;
//    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
//    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
//    private final JwtTokenProvider jwtTokenProvider;
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(AbstractHttpConfigurer::disable)
//                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//                .formLogin(AbstractHttpConfigurer::disable)
//                .httpBasic(AbstractHttpConfigurer::disable)
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(
//                                "/", "/error", "/favicon.ico",
//                                "/login/**",    // ex. /login/oauth2/google
//                                "/oauth2/**",   // ex. /oauth2/code/google
//                                "/logout"
//                        ).permitAll()
//                        .requestMatchers("/api/login/user").authenticated()
//                        .requestMatchers(HttpMethod.GET, "/api/some-public-resource").permitAll()
//                        .anyRequest().authenticated()
//                )
//
//                .oauth2Login(oauth2 -> oauth2
//                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
//                        .successHandler(oAuth2LoginSuccessHandler)
//                        .failureHandler(oAuth2LoginFailureHandler)
//                )
//
//                .logout(logout -> logout
//                        .logoutUrl("/api/logout")
//                        // HttpStatus.OK 객체를 전달
//                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK))
//                        .deleteCookies(jwtTokenProvider.getJwtCookieName(), "JSESSIONID", "email", "username", "is_seller")
//                        .invalidateHttpSession(true)
//                        .clearAuthentication(true)
//                )
//                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
//
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
//        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
//        configuration.setAllowedHeaders(List.of("*"));
//        configuration.setAllowCredentials(true);
//        configuration.setMaxAge(3600L);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }
//}

package capstone.config;

import capstone.security.CustomLogoutSuccessHandler;
import capstone.security.jwt.JwtAuthenticationFilter;
import capstone.security.jwt.JwtTokenProvider;
import capstone.security.oauth2.CustomOAuth2UserService;
import capstone.security.oauth2.OAuth2LoginFailureHandler;
import capstone.security.oauth2.OAuth2LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomLogoutSuccessHandler customLogoutSuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    // 건들면 X
                    "/",
                    "/oauth2/authorization/**",
                    "/auth/**",
                    "/api/auth/**",
                    "/login/oauth2/code/**",    // 콜백 경로도 허용
                    "/login/**",
                    "/error",
                    "/favicon.ico",

                    // 공통
                    "/api/qrCode/**",

                    // 판매자 사이트 부분
                    "/api/buyer/products/**",
                    "/api/buyer/products/search/**",
                    "/api/buyer/stores/**",
                    "/api/buyer/maps/**",
                    "/api/buyer/pay/**",
                    "/api/buyer/order/**",
                    "/api/buyer/review/**",

                    // 구매자 사이트 부분
                    "/api/files/**",
                    "/api/seller/**",
                    "/api/seller/stores/**",
                    "/api/seller/tax/**",
                    "/api/sgis/**"     // [추가] 지도 관련 API
                ).permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/some-public-resource").permitAll()
                .anyRequest().authenticated()
            )

            // XHR는 /login 으로 리다이렉트 대신 401을 주도록
            .exceptionHandling(e -> e
                .authenticationEntryPoint(
                    new org.springframework.security.web.authentication.HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)
                )
            )

            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                .successHandler(oAuth2LoginSuccessHandler)
                .failureHandler(oAuth2LoginFailureHandler)
            )

            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler(customLogoutSuccessHandler)
                .invalidateHttpSession(true)
                .clearAuthentication(true)
            )
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("https://dongnekok.shop", "http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
