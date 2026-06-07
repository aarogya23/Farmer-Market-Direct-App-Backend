package com.commerce.FarmerDirectMarkert.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    public void sendPasswordResetEmail(
            String recipientEmail,
            String fullName,
            String resetToken) {

        try {

            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(recipientEmail);
            helper.setSubject("Password Reset Request");

            String html = """
                    <!DOCTYPE html>
                    <html>
                    <body style="font-family: Arial, sans-serif;">

                        <h2>Hello %s,</h2>

                        <p>
                            We received a request to reset your password.
                        </p>

                        <p>
                            Use the following reset token:
                        </p>

                        <div style="
                                padding:15px;
                                background:#f5f5f5;
                                border-radius:8px;
                                font-size:18px;
                                font-weight:bold;">
                            %s
                        </div>

                        <br>

                        <p>
                            This token will expire in 15 minutes.
                        </p>

                        <p>
                            If you didn't request this password reset,
                            please ignore this email.
                        </p>

                        <br>

                        <p>
                            Farmer Direct Market Team
                        </p>

                    </body>
                    </html>
                    """.formatted(fullName, resetToken);

            helper.setText(html, true);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
