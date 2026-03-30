package com.example.demo.web;

import com.example.demo.service.QaService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(QaService.NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView notFound(QaService.NotFoundException ex) {
        ModelAndView mv = new ModelAndView("error-not-found");
        mv.addObject("message", ex.getMessage());
        return mv;
    }
}
