package com.example.demo.web;

import com.example.demo.service.QaService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("questions", qaService.listQuestions());
            model.addAttribute("leaderboard", qaService.leaderboard());
            return "index";
        }
        var q = qaService.createQuestion(form.getTitle(), form.getBody(), form.getAskerName());
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
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("question", qaService.getQuestion(id));
            return "question";
        }
        qaService.addAnswer(id, form.getBody(), form.getAnswererName());
        return "redirect:/questions/" + id + "?answered=1";
    }

    @GetMapping("/leaderboard")
    public String leaderboard(Model model) {
        model.addAttribute("leaderboard", qaService.leaderboard());
        return "leaderboard";
    }
}
