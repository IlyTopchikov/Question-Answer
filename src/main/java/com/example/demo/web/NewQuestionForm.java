package com.example.demo.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewQuestionForm {

    @NotBlank(message = "Укажите заголовок")
    @Size(max = 500)
    private String title;

    @NotBlank(message = "Напишите текст вопроса")
    @Size(max = 4000)
    private String body;

    @NotBlank(message = "Как вас зовут?")
    @Size(max = 200)
    private String askerName;
}
