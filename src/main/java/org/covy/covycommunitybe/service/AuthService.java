package org.covy.covycommunitybe.service;

import jakarta.servlet.http.HttpSession;
import org.covy.covycommunitybe.model.User;
import org.covy.covycommunitybe.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // ✅ 기존 로그인 (이메일 & 비밀번호)
    public boolean login(String email, String password, HttpSession session) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                session.setAttribute("user", user);
                return true;
            }
        }
        return false;
    }

    // ✅ 카카오 로그인 (자동 회원가입 & 로그인)
    // ✅ 카카오 로그인 (자동 회원가입 & 로그인)
    public boolean loginWithKakao(Long kakaoId, String nickname, String profileImage, HttpSession session) {
        Optional<User> existingUser = userRepository.findByKakaoId(kakaoId);
        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get(); // ✅ 기존 유저 로그인
        } else {
            user = new User();
            user.setKakaoId(kakaoId);
            user.setUsername(nickname);
            user.setImage(profileImage);

            // 임시 이메일 생성 (예: kakao_12345@example.com)
            String email = "kakao_" + kakaoId + "@example.com";
            user.setEmail(email);

            // 소셜 로그인 사용자 비밀번호 기본값 설정 (일반 로그인과 구분하기 위한 값)
            // 실제 사용시에는 랜덤 값이나 별도의 처리가 필요할 수 있습니다.
            String defaultPassword = "defaultPassword";
            user.setPassword(passwordEncoder.encode(defaultPassword));

            userRepository.save(user);
        }

        // ✅ 로그인 세션 설정
        session.setAttribute("user", user);
        return true;
    }

}
