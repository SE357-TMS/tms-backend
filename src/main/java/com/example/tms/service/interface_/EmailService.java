package com.example.tms.service.interface_;

public interface EmailService {
    
    /**
     * Send password reset email with token
     * @param toEmail User's email
     * @param userName User's name
     * @param resetToken Reset token
     * @param resetUrl Full reset URL
     */
    void sendPasswordResetEmail(String toEmail, String userName, String resetToken, String resetUrl);
    
    /**
     * Send password changed confirmation email
     * @param toEmail User's email
     * @param userName User's name
     */
    void sendPasswordChangedEmail(String toEmail, String userName);
}
