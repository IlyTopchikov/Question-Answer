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
        setImage(q, image);
        return questionRepository.save(q);
    }

    @Transactional
    public Question editQuestion(Long id, String title, String body, String username, boolean isAdmin, MultipartFile image) {
        Question q = questionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Вопрос не найден"));
        checkOwner(q.getOwner(), username, isAdmin);
        q.setTitle(title.trim());
        q.setBody(body.trim());
        if (image != null && !image.isEmpty()) setImage(q, image);
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
        if (image != null && !image.isEmpty()) setAnswerImage(a, image);
        question.getAnswers().add(a);
        awardPoints(username);
        questionRepository.save(question);
        return a;
    }

    @Transactional
    public Answer editAnswer(Long answerId, String body, String username, boolean isAdmin, MultipartFile image) {
        Answer a = answerRepository.findById(answerId)
                .orElseThrow(() -> new NotFoundException("Ответ не найден"));
        checkOwner(a.getOwner(), username, isAdmin);
        a.setBody(body.trim());
        if (image != null && !image.isEmpty()) setAnswerImage(a, image);
        return answerRepository.save(a);
    }

    @Transactional
    public void markSolved(Long questionId, String username, boolean isAdmin) {
        Question q = questionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException("Вопрос не найден"));
        checkOwner(q.getOwner(), username, isAdmin);
        q.setSolved(!q.isSolved());
        questionRepository.save(q);
    }

    @Transactional
    public void deleteQuestion(Long questionId, String username, boolean isAdmin) {
        Question q = questionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException("Вопрос не найден"));
        checkOwner(q.getOwner(), username, isAdmin);
        questionRepository.delete(q);
    }

    @Transactional
    public void deleteAnswer(Long answerId, String username, boolean isAdmin) {
        Answer a = answerRepository.findById(answerId)
                .orElseThrow(() -> new NotFoundException("Ответ не найден"));
        checkOwner(a.getOwner(), username, isAdmin);
        deductPoints(a.getAnswererName());
        answerRepository.delete(a);
    }

    @Transactional
    public void setPoints(String username, int points) {
        userScoreRepository.findByNameIgnoreCase(username)
                .ifPresentOrElse(
                        score -> score.setPoints(Math.max(0, points)),
                        () -> { if (points > 0) userScoreRepository.save(new UserScore(username, points)); }
                );
    }

    @Transactional
    public void resetPoints(String username) {
        userScoreRepository.findByNameIgnoreCase(username)
                .ifPresent(score -> score.setPoints(0));
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

    public List<String> getBadges(String username) {
        int answers = userScoreRepository.findByNameIgnoreCase(username)
                .map(UserScore::getPoints).orElse(0);
        var badges = new java.util.ArrayList<String>();
        if (answers >= 1)  badges.add("🎯 Первый ответ");
        if (answers >= 5)  badges.add("✍️ Активный участник");
        if (answers >= 10) badges.add("💡 Помощник");
        if (answers >= 25) badges.add("🌟 Эксперт сообщества");
        if (answers >= 50) badges.add("🏅 Легенда");
        return badges;
    }

    private void awardPoints(String name) {
        userScoreRepository.findByNameIgnoreCase(name)
                .ifPresentOrElse(
                        s -> s.setPoints(s.getPoints() + pointsPerAnswer),
                        () -> userScoreRepository.save(new UserScore(name, pointsPerAnswer)));
    }

    private void deductPoints(String name) {
        userScoreRepository.findByNameIgnoreCase(name)
                .ifPresent(s -> s.setPoints(Math.max(0, s.getPoints() - pointsPerAnswer)));
    }

    private void checkOwner(AppUser owner, String username, boolean isAdmin) {
        if (!isAdmin && (owner == null || !owner.getUsername().equals(username))) {
            throw new SecurityException("Нет прав");
        }
    }

    private void setImage(Question q, MultipartFile image) {
        if (image == null || image.isEmpty()) return;
        try { q.setImageData(image.getBytes()); q.setImageType(image.getContentType()); }
        catch (IOException e) { throw new RuntimeException("Ошибка загрузки изображения", e); }
    }

    private void setAnswerImage(Answer a, MultipartFile image) {
        if (image == null || image.isEmpty()) return;
        try { a.setImageData(image.getBytes()); a.setImageType(image.getContentType()); }
        catch (IOException e) { throw new RuntimeException("Ошибка загрузки изображения", e); }
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) { super(message); }
    }
}
