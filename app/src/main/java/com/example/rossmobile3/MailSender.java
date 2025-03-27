package com.example.rossmobile3;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailSender {
    private final String email;  // Sender email
    private final String password;  // App password
    private final Session session;  // JavaMail session

    public MailSender(String email, String password) {
        this.email = email;
        this.password = password;

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");  // Gmail SMTP Server
        props.put("mail.smtp.port", "465");  // SMTP Port
        props.put("mail.smtp.auth", "true");  // Enable authentication
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(email, password);
            }
        });
    }

    public void sendEmail(String recipient, String subject, String messageBody) throws MessagingException {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(email));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
        message.setSubject(subject);
        message.setText(messageBody);

        Transport.send(message);
    }
}
