package com.example.demo.web;

import com.example.demo.service.QaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class QaController {

    private final QaService qaService;

    public QaController(QaService qaService) {
        this.qaService = qaService;
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
        String username = auth.getName();
        var q = qaService.createQuestion(form.getTitle(), form.getBody(), username, image);
        return "redirect:/questions/" + q.getId();
    }

    @GetMapping("/questions/{id}")
    public String question(@PathVariable Long id, Model model) {
        model.addAttribute("question", qaService.getQuestion(id));
        model.addAttribute("newAnswer", new NewAnswerForm());
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

    @PostMapping("/questions/{id}/delete")
    public String deleteQuestion(@PathVariable Long id, Authentication auth) {
        boolean isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        qaService.deleteQuestion(id, auth.getName(), isAdmin);
        return "redirect:/";
    }

    @PostMapping("/answers/{id}/delete")
    public String deleteAnswer(@PathVariable Long id,
                               @RequestParam("questionId") Long questionId,
                               Authentication auth) {
        boolean isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        qaService.deleteAnswer(id, auth.getName(), isAdmin);
        return "redirect:/questions/" + questionId;
    }

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
        // Load answer directly
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
