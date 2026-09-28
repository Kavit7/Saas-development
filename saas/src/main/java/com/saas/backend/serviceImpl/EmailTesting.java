package com.saas.backend.serviceImpl;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class EmailTesting {
   
    private final JavaMailSender mailSender;
public void sendEmail(String email){
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom("saasreservation@gmail.com");
    message.setTo(email);
    message.setText("This is Testing email");

    mailSender.send(message);
}
    
}

