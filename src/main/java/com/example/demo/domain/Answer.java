package com.example.demo.domain;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String content;
    
    // НОВЫЕ ПОЛЯ ↓↓↓
    private String imageUrl;  // для фото
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;  // вместо String author
    
    private LocalDateTime createdAt;
    // НОВЫЕ ПОЛЯ ↑↑↑
    
    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;
    
    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    // НОВЫЕ ГЕТТЕРЫ/СЕТТЕРЫ ↓↓↓
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    // НОВЫЕ ГЕТТЕРЫ/СЕТТЕРЫ ↑↑↑
    
    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
