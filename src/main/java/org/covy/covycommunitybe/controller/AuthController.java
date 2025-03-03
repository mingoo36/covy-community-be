package org.covy.covycommunitybe.controller;


import jakarta.servlet.http.HttpSession;

import org.covy.covycommunitybe.model.User;
import org.covy.covycommunitybe.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;
    private static final String MESSAGE = "message";

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // 로그인 API
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

    // 로그아웃 API
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        authService.logout(session);
        return ResponseEntity.ok(Map.of(MESSAGE, "로그아웃 성공"));
    }

    // 세션쳌
    @GetMapping("/check-session")
    public ResponseEntity<?> checkSession(HttpSession session) {
        User user = (User) session.getAttribute("user");

        if (user == null) {
            return ResponseEntity.status(401).body(Map.of(MESSAGE, "세션이 만료되었습니다."));
        }

        return ResponseEntity.ok(Map.of(
                "user", Map.of(
                        "id", user.getUserId(),
                        "email", user.getEmail(),
                        "username", user.getUsername(),
                        "image", user.getImage()
                )
        ));
    }
}


