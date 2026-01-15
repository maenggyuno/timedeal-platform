//package capstone.security.oauth2;
//
//import capstone.domain.User; // User 클래스의 패키지 경로가 맞는지 확인
//import capstone.repository.LoginJpaRepository; // UserRepository 패키지 경로 확인
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
//import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
//import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
//import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
//import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Collections;
//import java.util.HashMap; // HashMap import 추가
//import java.util.Map;
//import java.util.Optional;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
//
//    private final LoginJpaRepository loginJpaRepository;
//
//    @Override
//    @Transactional
//    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
//        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
//        OAuth2User oAuth2User = delegate.loadUser(userRequest);
//
//        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // "google", "naver" 등
//
//        Map<String, Object> attributes = oAuth2User.getAttributes();
//        Map<String, Object> socialAttributes; // 실제 사용자 정보를 담을 Map
//
//        String socialId;
//        String email = null;
//        String name = null;
//
//        if (registrationId.equalsIgnoreCase("naver")) {
//            // 네이버 응답은 "response" 객체 안에 실제 사용자 정보가 있음
//            if (attributes.containsKey("response")) {
//                socialAttributes = (Map<String, Object>) attributes.get("response");
//                socialId = (String) socialAttributes.get("id");
//                email = (String) socialAttributes.get("email");
//                name = (String) socialAttributes.get("name");
//
//            } else {
//                log.error("Naver OAuth2 response does not contain 'response' attribute. Attributes: {}", attributes);
//                throw new OAuth2AuthenticationException("Invalid Naver user info response");
//            }
//
//        } else if (registrationId.equalsIgnoreCase("google")) {
//            socialAttributes = new HashMap<>(attributes);
//            socialId = String.valueOf(socialAttributes.get("sub"));
//            email = (String) socialAttributes.get("email");
//            name = (String) socialAttributes.get("name");
//
//        } else {
//            log.error("Unsupported registrationId: {}", registrationId);
//            throw new OAuth2AuthenticationException("Unsupported OAuth2 provider: " + registrationId);
//        }
//
//        log.info("OAuth2 User Info from provider: registrationId={}, socialId={}, email={}, name={}",
//                registrationId, socialId, email, name);
//
//        // User 엔티티 저장 또는 업데이트 (기존 saveOrUpdate 메소드 활용)
//        User user = saveOrUpdate(registrationId, socialId, name, email);
//
//        Map<String, Object> customAttributes = new HashMap<>();
//        customAttributes.putAll(socialAttributes); // 소셜 프로바이더로부터 받은 속성들
//        customAttributes.put("userId", user.getUserId());
//        customAttributes.put("registrationId", registrationId);
//
//        // 로그 메시지 업데이트
//        log.info("Custom attributes for DefaultOAuth2User: userId={}, name={}, email={}",
//                user.getUserId(), user.getName(), user.getEmail());
//
//        return new DefaultOAuth2User(
//                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
//                customAttributes,
//                "userId");
//    }
//
//
//    private User saveOrUpdate(String socialLoginPlatform, String socialLoginId, String nameFromProvider, String emailFromProvider) {
//        Optional<User> userOptional = loginJpaRepository.findBySocialLoginPlatformAndSocialLoginId(socialLoginPlatform, socialLoginId);
//        User user;
//
//        if (userOptional.isPresent()) {
//            user = userOptional.get();
//            user.update(nameFromProvider, emailFromProvider);
//            log.info("Existing user updated: userId={}, email={}", user.getUserId(), user.getEmail());
//        } else {
//            Optional<User> emailUserOptional = loginJpaRepository.findByEmail(emailFromProvider);
//            user = buildNewUser(socialLoginPlatform, socialLoginId, nameFromProvider, emailFromProvider);
//
//            log.info("New user process: name={}, email={}", user.getName(), user.getEmail());
//        }
//        return loginJpaRepository.save(user);
//    }
//
//    // 새로운 사용자 생성
//    private User buildNewUser(String socialLoginPlatform, String socialLoginId, String name, String email) {
//        return User.builder()
//                .socialLoginPlatform(socialLoginPlatform)
//                .socialLoginId(socialLoginId)
//                .name(name)
//                .email(email)
////                .isSeller(false)
//                .build();
//    }
//}

package capstone.security.oauth2;

import capstone.domain.User;
import capstone.repository.LoginJpaRepository;
import capstone.security.dto.UserPrincipal; // ✅ UserPrincipal import
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final LoginJpaRepository loginJpaRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        Map<String, Object> socialAttributes;

        String socialId;
        String email = null;
        String name = null;

        if (registrationId.equalsIgnoreCase("naver")) {
            if (attributes.containsKey("response")) {
                socialAttributes = (Map<String, Object>) attributes.get("response");
                socialId = (String) socialAttributes.get("id");
                email = (String) socialAttributes.get("email");
                name = (String) socialAttributes.get("name");
            } else {
                throw new OAuth2AuthenticationException("Invalid Naver user info response");
            }
        } else if (registrationId.equalsIgnoreCase("google")) {
            socialAttributes = attributes;
            socialId = String.valueOf(socialAttributes.get("sub"));
            email = (String) socialAttributes.get("email");
            name = (String) socialAttributes.get("name");
        } else {
            throw new OAuth2AuthenticationException("Unsupported OAuth2 provider: " + registrationId);
        }

        // DB에 사용자 정보 저장 또는 업데이트
        User user = saveOrUpdate(registrationId, socialId, name, email);

        return UserPrincipal.create(user, oAuth2User.getAttributes());
    }

    private User saveOrUpdate(String socialLoginPlatform, String socialLoginId, String name, String email) {
        Optional<User> userOptional = loginJpaRepository.findBySocialLoginPlatformAndSocialLoginId(socialLoginPlatform, socialLoginId);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            user.update(name, email);
        } else {
            user = buildNewUser(socialLoginPlatform, socialLoginId, name, email);
        }
        return loginJpaRepository.save(user);
    }

    private User buildNewUser(String socialLoginPlatform, String socialLoginId, String name, String email) {
        return User.builder()
                .socialLoginPlatform(socialLoginPlatform)
                .socialLoginId(socialLoginId)
                .name(name)
                .email(email)
                .build();
    }
}