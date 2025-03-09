package org.covy.covycommunitybe.controller;

import jakarta.servlet.http.HttpSession;
import org.covy.covycommunitybe.model.User;
import org.covy.covycommunitybe.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api") // ✅ 백엔드 엔드포인트 "/auth" 유지
public class AuthController {

    private final AuthService authService;
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String MESSAGE = "message";

    @Value("${kakao.api_key}")
    private String kakaoApiKey;

    @Value("${kakao.redirect_uri}")
    private String redirectUri;

    @Value("${kakao.token_uri}")
    private String tokenRequestUrl;

    @Value("${kakao.user_info_uri}")
    private String userInfoUrl;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ✅ 1. 카카오 로그인 URL 반환 (프론트에서 이 URL로 리다이렉트)
    @GetMapping("/oauth/kakao")
    public ResponseEntity<String> getKakaoLoginUrl() {
        String kakaoAuthUrl = "https://kauth.kakao.com/oauth/authorize" +
                "?client_id=" + kakaoApiKey +
                "&redirect_uri=" + redirectUri +
                "&response_type=code";
        return ResponseEntity.ok(kakaoAuthUrl);
    }

    // ✅ 2. 카카오 로그인 후 콜백 처리 (액세스 토큰 요청 + 사용자 정보 가져오기)
    @GetMapping("/oauth/kakao/callback")
    public RedirectView kakaoCallback(@RequestParam("code") String code, HttpSession session) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoApiKey);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenRequestUrl, request, Map.class);

        Map<String, Object> responseBody = response.getBody();
        String accessToken = responseBody.get("access_token").toString();

        // ✅ 카카오 사용자 정보 요청
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);
        HttpEntity<String> userRequest = new HttpEntity<>(userHeaders);
        ResponseEntity<Map> userResponse = restTemplate.exchange(userInfoUrl, HttpMethod.GET, userRequest, Map.class);

        Map<String, Object> kakaoAccount = (Map<String, Object>) userResponse.getBody().get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        Long kakaoId = Long.valueOf(userResponse.getBody().get("id").toString());
        String nickname = profile.get("nickname").toString();
        String profileImage = profile.get("profile_image_url").toString();

        // ✅ 백엔드에서 로그인 처리 & 자동 회원가입
        boolean isAuthenticated = authService.loginWithKakao(kakaoId, nickname, profileImage, session);

        if (isAuthenticated) {
            return new RedirectView("http://localhost:5050/community.html");
        } else {
            return new RedirectView("http://localhost:5050/login?error=true");
        }
    }

    // ✅ 3. 일반 로그인 (이메일 & 비밀번호)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest, HttpSession session) {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");

        boolean isAuthenticated = authService.login(email, password, session);

        if (isAuthenticated) {
            User user = (User) session.getAttribute("user");
            return ResponseEntity.ok(Map.of(
                    MESSAGE, "로그인 성공",
                    "user", Map.of(
                            "userId", user.getUserId(),
                            "email", user.getEmail(),
                            "username", user.getUsername(),
                            "image", user.getImage()
                    )
            ));
        } else {
            return ResponseEntity.status(401).body(Map.of(MESSAGE, "이메일 또는 비밀번호가 올바르지 않습니다."));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "세션이 없습니다."));
        }

        // 클라이언트가 기대하는 형식에 맞춰 데이터를 구성합니다.
        Map<String, Object> userData = new HashMap<>();
        userData.put("user_id", user.getUserId());
        userData.put("email", user.getEmail());
        userData.put("username", user.getUsername());
        userData.put("image", user.getImage());

        return ResponseEntity.ok(Map.of("user", userData));
    }


}
