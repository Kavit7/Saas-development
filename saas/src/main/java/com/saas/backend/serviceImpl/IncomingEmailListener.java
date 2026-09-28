package com.saas.backend.serviceImpl;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.saas.backend.service.IncomingEmailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IncomingEmailListener {

    private final IncomingEmailService incomingEmailService;

    @Scheduled(fixedDelay = 30000)
    public void checkMailbox() {

        incomingEmailService.processNewEmails();

    }
}
