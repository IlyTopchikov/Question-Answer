package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "app_users")
@Getter @Setter @NoArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 200)
    private String username;

    @Column(nullable = false, unique = true, length = 200)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    public enum Role { USER, ADMIN }

    // Уровень на основе баллов
    public String getLevel() {
        int points = getTotalPoints();
        if (points >= 100) return "Мастер";
        if (points >= 50)  return "Эксперт";
        if (points >= 20)  return "Знаток";
        if (points >= 5)   return "Ученик";
        return "Новичок";
    }

    public String getLevelEmoji() {
        return switch (getLevel()) {
            case "Мастер"  -> "🏆";
            case "Эксперт" -> "⭐";
            case "Знаток"  -> "🔥";
            case "Ученик"  -> "📚";
            default        -> "🌱";
        };
    }

    // Транзиентное поле — заполняется из UserScore
    @Transient
    private int totalPoints;

    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int p) { this.totalPoints = p; }
}
