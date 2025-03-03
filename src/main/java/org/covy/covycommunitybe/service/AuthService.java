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
        this.passwordEncoder = new BCryptPasswordEncoder(); // 비밀번호 암호화/검증을 위한 BCrypt 사용
    }

    // 로그인 로직
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

    // 로그아웃 로직
    public void logout(HttpSession session) {
        session.invalidate();
    }
}

