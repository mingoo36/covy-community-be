package org.covy.covycommunitybe.repository;

import org.covy.covycommunitybe.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// JpaRepository<엔티티, id>
public interface UserRepository extends JpaRepository<User, Long> {

    // ✅ 이메일로 사용자 찾기 (이메일 로그인 시 필요)
    Optional<User> findByEmail(String email);

    // ✅ 카카오 ID로 사용자 찾기 (카카오 로그인 시 필요)
    Optional<User> findByKakaoId(Long kakaoId);

    // ✅ 닉네임(username)으로 사용자 찾기 (카카오 로그인 시 기존 계정과 연동할 때 필요)
    Optional<User> findByUsername(String username);
}
