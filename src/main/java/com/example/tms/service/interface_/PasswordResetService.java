package com.example.tms.service.interface_;

public interface PasswordResetService {
    
    /**
     * Send password reset email to user
     * @param email User's email
     */
    void sendPasswordResetEmail(String email);
    
    /**
     * Reset password using token
     * @param token Reset token
     * @param newPassword New password
     */
    void resetPassword(String token, String newPassword);
    
    /**
     * Validate reset token
     * @param token Reset token
     * @return true if valid, false otherwise
     */
    boolean validateToken(String token);
}

