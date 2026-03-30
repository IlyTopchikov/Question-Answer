package com.example.demo.repository;

import com.example.demo.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findAllByOrderByCreatedAtDesc();

    @Query("SELECT DISTINCT q FROM Question q LEFT JOIN FETCH q.answers WHERE q.id = :id")
    Optional<Question> findByIdWithAnswers(@Param("id") Long id);
}
