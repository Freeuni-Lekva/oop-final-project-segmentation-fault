package com.example.libraryproject.service;

public interface MailService {
    void sendEmail(String to, String subject, String content) throws Exception;
    void sendHtmlEmail(String to, String subject, String htmlContent) throws Exception;
}