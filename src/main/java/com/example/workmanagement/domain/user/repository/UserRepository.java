package com.example.workmanagement.domain.user.repository;

import com.example.workmanagement.domain.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findAllByNickname(String nickname);

    List<User> findAllByNicknameIn(Collection<String> nicknames);
}
