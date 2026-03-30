package com.example.demo.service;

import com.example.demo.domain.Answer;
import com.example.demo.domain.Question;
import com.example.demo.domain.UserScore;
import com.example.demo.repository.QuestionRepository;
import com.example.demo.repository.UserScoreRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QaService {

    private final QuestionRepository questionRepository;
    private final UserScoreRepository userScoreRepository;
    private final int pointsPerAnswer;

    public QaService(
            QuestionRepository questionRepository,
            UserScoreRepository userScoreRepository,
            @Value("${app.points.per-answer:1}") int pointsPerAnswer) {
        this.questionRepository = questionRepository;
        this.userScoreRepository = userScoreRepository;
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
    public Question createQuestion(String title, String body, String askerName) {
        Question q = new Question();
        q.setTitle(title.trim());
        q.setBody(body.trim());
        q.setAskerName(askerName.trim());
        return questionRepository.save(q);
    }

    @Transactional
    public Answer addAnswer(Long questionId, String body, String answererName) {
        Question question = questionRepository
                .findById(questionId)
                .orElseThrow(() -> new NotFoundException("Вопрос не найден"));
        Answer a = new Answer();
        a.setQuestion(question);
        a.setBody(body.trim());
        a.setAnswererName(answererName.trim());
        question.getAnswers().add(a);
        awardPoints(answererName.trim());
        questionRepository.save(question);
        return a;
    }

    private void awardPoints(String answererName) {
        String key = answererName;
        userScoreRepository
                .findByNameIgnoreCase(key)
                .ifPresentOrElse(
                        score -> score.setPoints(score.getPoints() + pointsPerAnswer),
                        () -> userScoreRepository.save(new UserScore(key, pointsPerAnswer)));
    }

    @Transactional(readOnly = true)
    public List<UserScore> leaderboard() {
        return userScoreRepository.findAll().stream()
                .sorted((a, b) -> {
                    int cmp = Integer.compare(b.getPoints(), a.getPoints());
                    if (cmp != 0) {
                        return cmp;
                    }
                    return a.getName().compareToIgnoreCase(b.getName());
                })
                .toList();
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) {
            super(message);
        }
    }
}
