package com.example.tms.service.impl;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.tms.service.interface_.EmailService;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;

    @Value("${sendgrid.from.name}")
    private String fromName;

    @Override
    public void sendPasswordResetEmail(String toEmail, String userName, String resetToken, String resetUrl) {
        log.info("=== SENDING PASSWORD RESET EMAIL ===");
        log.info("To: {}", toEmail);
        log.info("From: {} <{}>", fromName, fromEmail);
        log.info("Reset URL: {}", resetUrl);
        
        Email from = new Email(fromEmail, fromName);
        Email to = new Email(toEmail);
        String subject = "[TMS Tourism] Password Reset Request";
        
        // Plain text version (helps avoid spam filters)
        String plainTextContent = buildPasswordResetPlainText(userName, resetUrl);
        Content textContent = new Content("text/plain", plainTextContent);
        
        // HTML email content
        String htmlContent = buildPasswordResetEmailContent(userName, resetToken, resetUrl);
        Content htmlContentObj = new Content("text/html", htmlContent);
        
        Mail mail = new Mail(from, subject, to, textContent);
        mail.addContent(htmlContentObj); // Add HTML as alternative
        
        sendEmail(mail, "password reset");
        
        log.info("=== EMAIL SENDING PROCESS COMPLETED ===");
    }

    @Override
    public void sendPasswordChangedEmail(String toEmail, String userName) {
        Email from = new Email(fromEmail, fromName);
        Email to = new Email(toEmail);
        String subject = "[TMS Tourism] Password Changed Successfully";
        
        // Plain text version
        String plainTextContent = buildPasswordChangedPlainText(userName);
        Content textContent = new Content("text/plain", plainTextContent);
        
        // HTML email content
        String htmlContent = buildPasswordChangedEmailContent(userName);
        Content htmlContentObj = new Content("text/html", htmlContent);
        
        Mail mail = new Mail(from, subject, to, textContent);
        mail.addContent(htmlContentObj); // Add HTML as alternative
        
        sendEmail(mail, "password changed confirmation");
    }

    /**
     * Generic method to send email via SendGrid
     */
    private void sendEmail(Mail mail, String emailType) {
        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            Response response = sg.api(request);
            
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("✅ Successfully sent {} email. Status code: {}", emailType, response.getStatusCode());
                log.info("📧 Email sent! If using free SendGrid tier, the email may arrive in spam/junk folder.");
                log.info("💡 TIP: Check spam folder and mark as 'Not Spam' to receive future emails in inbox.");
            } else {
                log.error("Failed to send {} email. Status code: {}, Body: {}", 
                    emailType, response.getStatusCode(), response.getBody());
            }
            
        } catch (IOException e) {
            log.error("Error sending {} email: {}", emailType, e.getMessage(), e);
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }

    /**
     * Build plain text content for password reset email (helps avoid spam)
     */
    private String buildPasswordResetPlainText(String userName, String resetUrl) {
        return String.format("""
            Password Reset Request - TMS Tourism
            
            Hello %s,
            
            You requested to reset your password for your TMS Tourism account.
            
            To reset your password, please visit this link:
            %s
            
            This link will expire in 15 minutes.
            
            If you did not request this password reset, please ignore this email.
            Your password will remain unchanged.
            
            For security reasons:
            - Never share your password with anyone
            - This reset link can only be used once
            
            Best regards,
            TMS Tourism Management Team
            
            ---
            This is an automated message from TMS Tourism Management System.
            This is a student project for educational purposes.
            Please do not reply to this email.
            
            © 2025 TMS Tourism Management System
            """, userName, resetUrl);
    }

    /**
     * Build HTML content for password reset email
     */
    private String buildPasswordResetEmailContent(String userName, String resetToken, String resetUrl) {
        // Using String.format instead of .formatted() to avoid issues with # in CSS
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                    }
                    .container {
                        background-color: #f9f9f9;
                        border-radius: 10px;
                        padding: 30px;
                        box-shadow: 0 2px 5px rgba(0,0,0,0.1);
                    }
                    .header {
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        color: white;
                        padding: 20px;
                        border-radius: 10px 10px 0 0;
                        text-align: center;
                    }
                    .content {
                        background: white;
                        padding: 30px;
                        border-radius: 0 0 10px 10px;
                    }
                    .button {
                        display: inline-block;
                        padding: 15px 30px;
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        color: white !important;
                        text-decoration: none;
                        border-radius: 5px;
                        margin: 20px 0;
                        font-weight: bold;
                    }
                    .token-box {
                        background-color: #f0f0f0;
                        padding: 15px;
                        border-radius: 5px;
                        border-left: 4px solid #667eea;
                        margin: 20px 0;
                        word-break: break-all;
                    }
                    .warning {
                        color: #e74c3c;
                        font-size: 14px;
                        margin-top: 20px;
                    }
                    .footer {
                        text-align: center;
                        color: #666;
                        font-size: 12px;
                        margin-top: 30px;
                        padding-top: 20px;
                        border-top: 1px solid #ddd;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔐 Reset Your Password</h1>
                    </div>
                    <div class="content">
                        <h2>Hello %s,</h2>
                        <p>We received a request to reset your password for your TMS Tourism account.</p>
                        <p>To proceed with resetting your password, please click the button below:</p>
                        
                        <div style="text-align: center;">
                            <a href="%s" class="button">Reset My Password</a>
                        </div>
                        
                        <p>If the button doesn't work, copy and paste this link into your browser:</p>
                        <div class="token-box">
                            %s
                        </div>
                        
                        <p>This reset link will expire in <strong>15 minutes</strong> for security reasons.</p>
                        
                        <div class="warning">
                            <strong>Security Notice:</strong>
                            <ul>
                                <li>If you didn't request this, you can safely ignore this email</li>
                                <li>Your password will remain unchanged</li>
                                <li>This link can only be used once</li>
                            </ul>
                        </div>
                    </div>
                    <div class="footer">
                        <p><strong>TMS Tourism Management System</strong></p>
                        <p>Student Project - University of Information Technology</p>
                        <p>This is an automated email for educational purposes. Please do not reply.</p>
                        <p style="margin-top: 10px; color: #999;">© 2025 All rights reserved</p>
                    </div>
                </div>
            </body>
            </html>
            """, userName, resetUrl, resetUrl);
    }

    /**
     * Build plain text content for password changed email
     */
    private String buildPasswordChangedPlainText(String userName) {
        return String.format("""
            Password Changed Successfully - TMS Tourism
            
            Hello %s,
            
            This email confirms that your password has been successfully changed.
            
            You can now log in to your TMS Tourism account using your new password.
            
            If you did NOT make this change, please contact us immediately as your 
            account security may be compromised.
            
            Best regards,
            TMS Tourism Management Team
            
            ---
            This is an automated message from TMS Tourism Management System.
            This is a student project for educational purposes.
            Please do not reply to this email.
            
            © 2025 TMS Tourism Management System
            """, userName);
    }

    /**
     * Build HTML content for password changed confirmation email
     */
    private String buildPasswordChangedEmailContent(String userName) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                    }
                    .container {
                        background-color: #f9f9f9;
                        border-radius: 10px;
                        padding: 30px;
                        box-shadow: 0 2px 5px rgba(0,0,0,0.1);
                    }
                    .header {
                        background: linear-gradient(135deg, #11998e 0%%, #38ef7d 100%%);
                        color: white;
                        padding: 20px;
                        border-radius: 10px 10px 0 0;
                        text-align: center;
                    }
                    .content {
                        background: white;
                        padding: 30px;
                        border-radius: 0 0 10px 10px;
                    }
                    .success-icon {
                        font-size: 48px;
                        text-align: center;
                        margin: 20px 0;
                    }
                    .info-box {
                        background-color: #e8f5e9;
                        padding: 15px;
                        border-radius: 5px;
                        border-left: 4px solid #4caf50;
                        margin: 20px 0;
                    }
                    .warning {
                        color: #e74c3c;
                        font-size: 14px;
                        margin-top: 20px;
                        padding: 15px;
                        background-color: #ffebee;
                        border-radius: 5px;
                    }
                    .footer {
                        text-align: center;
                        color: #666;
                        font-size: 12px;
                        margin-top: 30px;
                        padding-top: 20px;
                        border-top: 1px solid #ddd;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>✅ Password Changed Successfully</h1>
                    </div>
                    <div class="content">
                        <div class="success-icon">🎉</div>
                        <h2>Hello %s,</h2>
                        <p>This email confirms that your password has been successfully changed.</p>
                        
                        <div class="info-box">
                            <strong>What this means:</strong>
                            <ul>
                                <li>Your password has been updated</li>
                                <li>You can now login with your new password</li>
                                <li>All previous sessions remain active</li>
                            </ul>
                        </div>
                        
                        <div class="warning">
                            <strong>Security Alert:</strong><br>
                            If you did NOT make this change, please contact our support team immediately.
                            Your account may be compromised.
                        </div>
                    </div>
                    <div class="footer">
                        <p><strong>TMS Tourism Management System</strong></p>
                        <p>Student Project - University of Information Technology</p>
                        <p>This is an automated email for educational purposes. Please do not reply.</p>
                        <p style="margin-top: 10px; color: #999;">© 2025 All rights reserved</p>
                    </div>
                </div>
            </body>
            </html>
            """, userName);
    }
}

