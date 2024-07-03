package com.example.authproject.service;

import com.example.authproject.dto.RegistrationRequest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class MailService {

    private final TokenService tokenService;
    private final JavaMailSender mailSender;

    public MailService(TokenService tokenService, JavaMailSender mailSender) {
        this.tokenService = tokenService;
        this.mailSender = mailSender;
    }

    public void sendConfirmation(RegistrationRequest user) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        String url = attributes.getRequest().getRequestURL().toString();

        String temp = url.substring(url.lastIndexOf("/") + 1);
        if (temp.equals("resend-confirmation")){
            url = url.substring(0, url.lastIndexOf("/"));
        }

        String token = tokenService.generateToken(user.email());

        String subject = "Подтверждение регистрации";
        String confirmationUrl = url + "/confirmation?token=" + token;
        String message = "Чтобы подтвердить регистрацию, перейдите по следующей ссылке: " + confirmationUrl;

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(user.email());
        email.setSubject(subject);
        email.setText(message);

        mailSender.send(email);
    }

    public void sendPasswordResetEmail(String email) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        String url = attributes.getRequest().getRequestURL().toString();

        String token = tokenService.generateToken(email);

        String subject = "Запрос на сброс пароля";
        String confirmationUrl = url + "/reset-password?token=" + token;
        String text = "Чтобы сбросить пароль, нажмите ссылку ниже:\n" + confirmationUrl;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }
}
