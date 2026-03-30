package com.example.demo.service;

import com.example.demo.domain.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class QaService {

    private final QuestionRepository questionRepository;
    private final UserScoreRepository userScoreRepository;
    private final AppUserRepository appUserRepository;
    private final AnswerRepository answerRepository;
    private final int pointsPerAnswer;

    public QaService(
            QuestionRepository questionRepository,
            UserScoreRepository userScoreRepository,
            AppUserRepository appUserRepository,
            AnswerRepository answerRepository,
            @Value("${app.points.per-answer:1}") int pointsPerAnswer) {
        this.questionRepository = questionRepository;
        this.userScoreRepository = userScoreRepository;
        this.appUserRepository = appUserRepository;
        this.answerRepository = answerRepository;
        this.pointsPerAnswer = pointsPerAnswer;
    }

    @Transactional(readOnly = true)
    public List<Question> listQuestions() {
        return questionRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Question getQuestion(Long id) {
        return questionRepository
                .findByIdWithAnswers(id)
                .orElseThrow(() -> new NotFoundException("Вопрос не найден"));
    }

    @Transactional
    public Question createQuestion(String title, String body, String username, MultipartFile image) {
        AppUser owner = appUserRepository.findByUsername(username).orElse(null);
        Question q = new Question();
        q.setTitle(title.trim());
        q.setBody(body.trim());
        q.setAskerName(username);
        q.setOwner(owner);
        if (image != null && !image.isEmpty()) {
            try {
                q.setImageData(image.getBytes());
                q.setImageType(image.getContentType());
            } catch (IOException e) {
                throw new RuntimeException("Ошибка загрузки изображения", e);
            }
        }
        return questionRepository.save(q);
    }

    @Transactional
    public Answer addAnswer(Long questionId, String body, String username, MultipartFile image) {
        Question question = questionRepository
                .findById(questionId)
                .orElseThrow(() -> new NotFoundException("Вопрос не найден"));
        AppUser owner = appUserRepository.findByUsername(username).orElse(null);
        Answer a = new Answer();
        a.setQuestion(question);
        a.setBody(body.trim());
        a.setAnswererName(username);
        a.setOwner(owner);
        if (image != null && !image.isEmpty()) {
            try {
                a.setImageData(image.getBytes());
                a.setImageType(image.getContentType());
            } catch (IOException e) {
                throw new RuntimeException("Ошибка загрузки изображения", e);
            }
        }
        question.getAnswers().add(a);
        awardPoints(username);
        questionRepository.save(question);
        return a;
    }

    @Transactional
    public void deleteQuestion(Long questionId, String username, boolean isAdmin) {
        Question q = questionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException("Вопрос не найден"));
        if (!isAdmin && (q.getOwner() == null || !q.getOwner().getUsername().equals(username))) {
            throw new SecurityException("Нет прав для удаления");
        }
        questionRepository.delete(q);
    }

    @Transactional
    public void deleteAnswer(Long answerId, String username, boolean isAdmin) {
        Answer a = answerRepository.findById(answerId)
                .orElseThrow(() -> new NotFoundException("Ответ не найден"));
        if (!isAdmin && (a.getOwner() == null || !a.getOwner().getUsername().equals(username))) {
            throw new SecurityException("Нет прав для удаления");
        }
        Long questionId = a.getQuestion().getId();
        answerRepository.delete(a);
    }

    private void awardPoints(String answererName) {
        userScoreRepository
                .findByNameIgnoreCase(answererName)
                .ifPresentOrElse(
                        score -> score.setPoints(score.getPoints() + pointsPerAnswer),
                        () -> userScoreRepository.save(new UserScore(answererName, pointsPerAnswer)));
    }

    @Transactional(readOnly = true)
    public List<UserScore> leaderboard() {
        return userScoreRepository.findAll().stream()
                .sorted((a, b) -> {
                    int cmp = Integer.compare(b.getPoints(), a.getPoints());
                    return cmp != 0 ? cmp : a.getName().compareToIgnoreCase(b.getName());
                })
                .toList();
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) { super(message); }
    }
}
