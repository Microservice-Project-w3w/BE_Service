package com.equipmentrental.identity.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationCode(String email, String code, String purpose) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);

        if ("RESET_PASSWORD".equals(purpose)) {
            message.setSubject("Ma dat lai mat khau");
            message.setText(
                "Ma dat lai mat khau cua ban la: " + code +
                "\nMa co hieu luc trong 15 phut."
            );
        } else {
            message.setSubject("Ma xac minh email");
            message.setText(
                "Ma xac minh cua ban la: " + code +
                "\nMa co hieu luc trong 15 phut."
            );
        }

        mailSender.send(message);
    }
}
