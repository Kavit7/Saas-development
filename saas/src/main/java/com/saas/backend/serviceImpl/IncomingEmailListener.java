package com.saas.backend.serviceImpl;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.saas.backend.service.IncomingEmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncomingEmailListener {

    private final IncomingEmailService incomingEmailService;

    @Scheduled(fixedDelay = 30000)
    public void checkMailbox() {
        log.debug("Mailbox scheduler polling for new lodge messages...");
        try {
            incomingEmailService.processNewEmails();
        } catch (Exception e) {
            log.error("Mailbox scheduler poll encountered an error: {}", e.getMessage(), e);
        }
    }
}
