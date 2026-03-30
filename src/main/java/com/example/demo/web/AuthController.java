package com.example.demo.web;

import com.example.demo.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("form", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("form") RegisterForm form,
            BindingResult result,
            Model model) {
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error", "Пароли не совпадают");
        }
        if (result.hasErrors()) {
            return "register";
        }
        try {
            authService.register(form.getUsername(), form.getEmail(), form.getPassword());
            return "redirect:/login?registered";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @Data
    public static class RegisterForm {
        @NotBlank(message = "Укажите имя пользователя")
        @Size(min = 3, max = 50, message = "От 3 до 50 символов")
        private String username;

        @NotBlank(message = "Укажите email")
        @Email(message = "Некорректный email")
        private String email;

        @NotBlank(message = "Укажите пароль")
        @Size(min = 6, message = "Минимум 6 символов")
        private String password;

        @NotBlank(message = "Подтвердите пароль")
        private String confirmPassword;
    }
}
