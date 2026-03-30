package com.example.demo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_scores", uniqueConstraints = @UniqueConstraint(name = "uk_user_score_name", columnNames = "name"))
@Getter
@Setter
@NoArgsConstructor
public class UserScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 200)
    private String name;

    @Column(nullable = false)
    private int points;

    public UserScore(String name, int points) {
        this.name = name;
        this.points = points;
    }
}
