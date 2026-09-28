package com.saas.backend.service;

import java.util.List;

import com.saas.backend.dto.IncomingMailMessage;

public interface MailboxClient {

    List<IncomingMailMessage> fetchUnreadEmails();
}
