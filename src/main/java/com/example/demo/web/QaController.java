package com.example.demo.web;

import com.example.demo.service.QaService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;

@Controller
public class QaController {

    private final QaService qaService;

    public QaController(QaService qaService) {
        this.qaService = qaService;
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @GetMapping({"/", "/index.html"})
    public String home(Model model) {
        model.addAttribute("questions", qaService.listQuestions());
        model.addAttribute("leaderboard", qaService.leaderboard());
        model.addAttribute("newQuestion", new NewQuestionForm());
        return "index";
    }

    @PostMapping("/questions")
    public String createQuestion(
            @Valid @ModelAttribute("newQuestion") NewQuestionForm form,
            BindingResult bindingResult,
            @RequestParam(value = "image", required = false) MultipartFile image,
            Authentication auth,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("questions", qaService.listQuestions());
            model.addAttribute("leaderboard", qaService.leaderboard());
            return "index";
        }
        var q = qaService.createQuestion(form.getTitle(), form.getBody(), auth.getName(), image);
        return "redirect:/questions/" + q.getId();
    }

    @GetMapping("/questions/{id}")
    public String question(@PathVariable Long id, Model model, Authentication auth) {
        var question = qaService.getQuestion(id);
        model.addAttribute("question", question);
        model.addAttribute("newAnswer", new NewAnswerForm());
        if (auth != null) {
            model.addAttribute("badges", qaService.getBadges(auth.getName()));
        }
        return "question";
    }

    @PostMapping("/questions/{id}/answers")
    public String addAnswer(
            @PathVariable Long id,
            @Valid @ModelAttribute("newAnswer") NewAnswerForm form,
            BindingResult bindingResult,
            @RequestParam(value = "image", required = false) MultipartFile image,
            Authentication auth,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("question", qaService.getQuestion(id));
            return "question";
        }
        qaService.addAnswer(id, form.getBody(), auth.getName(), image);
        return "redirect:/questions/" + id + "?answered=1";
    }

    // Редактирование вопроса
    @GetMapping("/questions/{id}/edit")
    public String editQuestionForm(@PathVariable Long id, Authentication auth, Model model) {
        var q = qaService.getQuestion(id);
        if (!isAdmin(auth) && (q.getOwner() == null || !q.getOwner().getUsername().equals(auth.getName()))) {
            return "redirect:/questions/" + id;
        }
        model.addAttribute("question", q);
        return "edit-question";
    }

    @PostMapping("/questions/{id}/edit")
    public String editQuestion(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String body,
            @RequestParam(value = "image", required = false) MultipartFile image,
            Authentication auth) {
        qaService.editQuestion(id, title, body, auth.getName(), isAdmin(auth), image);
        return "redirect:/questions/" + id;
    }

    // Редактирование ответа
    @GetMapping("/answers/{id}/edit")
    public String editAnswerForm(@PathVariable Long id, Authentication auth, Model model) {
        // Найдём вопрос через ответ
        var answers = qaService.listQuestions().stream()
                .flatMap(q -> q.getAnswers().stream())
                .filter(a -> a.getId().equals(id))
                .findFirst();
        // Просто перенаправим на страницу с параметром
        model.addAttribute("answerId", id);
        return "redirect:/";
    }

    @PostMapping("/answers/{id}/edit")
    public String editAnswer(
            @PathVariable Long id,
            @RequestParam String body,
            @RequestParam Long questionId,
            @RequestParam(value = "image", required = false) MultipartFile image,
            Authentication auth) {
        qaService.editAnswer(id, body, auth.getName(), isAdmin(auth), image);
        return "redirect:/questions/" + questionId;
    }

    // Отметить решённым
    @PostMapping("/questions/{id}/solve")
    public String markSolved(@PathVariable Long id, Authentication auth) {
        qaService.markSolved(id, auth.getName(), isAdmin(auth));
        return "redirect:/questions/" + id;
    }

    // Удаление
    @PostMapping("/questions/{id}/delete")
    public String deleteQuestion(@PathVariable Long id, Authentication auth) {
        qaService.deleteQuestion(id, auth.getName(), isAdmin(auth));
        return "redirect:/";
    }

    @PostMapping("/answers/{id}/delete")
    public String deleteAnswer(@PathVariable Long id,
                               @RequestParam("questionId") Long questionId,
                               Authentication auth) {
        qaService.deleteAnswer(id, auth.getName(), isAdmin(auth));
        return "redirect:/questions/" + questionId;
    }

    // Изображения
    @GetMapping("/questions/{id}/image")
    public ResponseEntity<byte[]> questionImage(@PathVariable Long id) {
        var q = qaService.getQuestion(id);
        if (q.getImageData() == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(q.getImageType()))
                .body(q.getImageData());
    }

    @GetMapping("/answers/{id}/image")
    public ResponseEntity<byte[]> answerImage(@PathVariable Long id) {
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/leaderboard")
    public String leaderboard(Model model) {
        model.addAttribute("leaderboard", qaService.leaderboard());
        return "leaderboard";
    }

    @Data
    public static class NewQuestionForm {
        @NotBlank(message = "Укажите заголовок")
        @Size(max = 500)
        private String title;

        @NotBlank(message = "Напишите текст вопроса")
        @Size(max = 4000)
        private String body;
    }

    @Data
    public static class NewAnswerForm {
        @NotBlank(message = "Напишите ответ")
        @Size(max = 4000)
        private String body;
    }
}
