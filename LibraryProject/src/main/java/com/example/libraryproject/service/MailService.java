package com.example.libraryproject.service;

import java.util.List;

public interface MailService {
    void sendEmail(List<String> to, String subject, String content) throws Exception;
    void sendHtmlEmail(List<String> to, String subject, String htmlContent) throws Exception;
}