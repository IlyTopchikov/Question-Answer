package com.example.demo.domain;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    private String content;
    
    // НОВЫЕ ПОЛЯ ↓↓↓
    private String imageUrl;  // для фото
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;  // вместо String author
    
    private LocalDateTime createdAt;
    // НОВЫЕ ПОЛЯ ↑↑↑
    
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    private List<Answer> answers;
    
    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
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
    
    public List<Answer> getAnswers() { return answers; }
    public void setAnswers(List<Answer> answers) { this.answers = answers; }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
