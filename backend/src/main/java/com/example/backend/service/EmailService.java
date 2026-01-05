package com.example.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private Environment env;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Async
    public void sendActivationEmail(String to, String activationToken) throws MailException {
        System.out.println("Sending activation email to: " + to);
        
        String activationLink = frontendUrl + "/activate?token=" + activationToken;
        
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(to);
        mail.setFrom(env.getProperty("spring.mail.username"));
        mail.setSubject("Activate Your Account");
        mail.setText(
            "Welcome!\n\n" +
            "Please click the following link to activate your account:\n\n" +
            activationLink + "\n\n" +
            "This link will expire in 24 hours.\n\n" +
            "If you didn't create this account, please ignore this email."
        );
        
        javaMailSender.send(mail);
        System.out.println("Activation email sent!");
    }
}
