package com.example.demo.repository;

import com.example.demo.domain.UserScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserScoreRepository extends JpaRepository<UserScore, Long> {

    Optional<UserScore> findByNameIgnoreCase(String name);
}
