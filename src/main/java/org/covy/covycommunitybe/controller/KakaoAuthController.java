package org.covy.covycommunitybe.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/oauth")
public class KakaoAuthController {

    @Value("${kakao.api_key}")
    private String kakaoApiKey;

    @Value("${kakao.redirect_uri}")
    private String redirectUri;

    @Value("${kakao.token_uri}")
    private String tokenRequestUrl;

    @Value("${kakao.user_info_uri}")
    private String userInfoUrl;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/kakao")
    public ResponseEntity<String> kakaoLogin() {
        String kakaoAuthUrl = "https://kauth.kakao.com/oauth/authorize" +
                "?client_id=" + kakaoApiKey +
                "&redirect_uri=" + redirectUri +
                "&response_type=code";
        return ResponseEntity.ok(kakaoAuthUrl);
    }

    // ✅ 2. 인가 코드로 액세스 토큰 요청
    @GetMapping("/kakao/callback")
    public RedirectView kakaoCallback(@RequestParam("code") String code) {
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

        // ✅ 프론트엔드로 리다이렉트하면서 액세스 토큰을 넘김
        return new RedirectView("http://localhost:5050/kakao-login-success?token=" + accessToken);
    }



    @GetMapping("/kakao/userinfo")
    public ResponseEntity<Map<String, Object>> getUserInfo(@RequestParam("token") String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken); // Authorization: Bearer {access_token}
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, Map.class);

        Map<String, Object> responseBody = response.getBody();
        Map<String, Object> kakaoAccount = (Map<String, Object>) responseBody.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("nickname", profile.get("nickname"));
        userInfo.put("profile_image", profile.get("profile_image_url"));

        return ResponseEntity.ok(userInfo);
    }


}

