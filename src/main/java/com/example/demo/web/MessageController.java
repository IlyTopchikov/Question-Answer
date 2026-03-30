package com.example.demo.web;

import com.example.demo.service.MessageService;
import com.example.demo.service.QaService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public String inbox(Authentication auth, Model model) {
        model.addAttribute("contacts", messageService.getContacts(auth.getName()));
        model.addAttribute("form", new SendForm());
        return "messages/inbox";
    }

    @GetMapping("/{username}")
    public String conversation(@PathVariable String username, Authentication auth, Model model) {
        try {
            model.addAttribute("messages", messageService.getConversation(auth.getName(), username));
            model.addAttribute("otherUsername", username);
            model.addAttribute("form", new SendForm());
            model.addAttribute("contacts", messageService.getContacts(auth.getName()));
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("contacts", messageService.getContacts(auth.getName()));
            model.addAttribute("form", new SendForm());
            return "messages/inbox";
        }
        return "messages/conversation";
    }

    @PostMapping("/send")
    public String send(
            @ModelAttribute SendForm form,
            Authentication auth,
            RedirectAttributes ra) {
        try {
            messageService.sendMessage(auth.getName(), form.getTo(), form.getBody());
            return "redirect:/messages/" + form.getTo();
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/messages";
        }
    }

    @Data
    public static class SendForm {
        @NotBlank
        @Size(max = 200)
        private String to;

        @NotBlank
        @Size(max = 4000)
        private String body;
    }
}
