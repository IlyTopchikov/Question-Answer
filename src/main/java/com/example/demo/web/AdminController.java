package com.example.demo.web;

import com.example.demo.service.QaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final QaService qaService;

    public AdminController(QaService qaService) {
        this.qaService = qaService;
    }

    @GetMapping
    public String adminPanel(Model model) {
        model.addAttribute("leaderboard", qaService.leaderboard());
        return "admin";
    }

    @PostMapping("/points/set")
    public String setPoints(@RequestParam String username,
                            @RequestParam int points,
                            RedirectAttributes ra) {
        try {
            qaService.setPoints(username, points);
            ra.addFlashAttribute("success", "Баллы обновлены для " + username);
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/points/reset")
    public String resetPoints(@RequestParam String username, RedirectAttributes ra) {
        qaService.resetPoints(username);
        ra.addFlashAttribute("success", "Баллы сброшены для " + username);
        return "redirect:/admin";
    }
}
