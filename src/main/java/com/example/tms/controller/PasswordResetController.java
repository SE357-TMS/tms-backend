package com.example.tms.controller;

import com.example.tms.dto.request.ForgotPasswordRequest;
import com.example.tms.dto.request.ResetPasswordRequest;
import com.example.tms.dto.response.ApiResponse;
import com.example.tms.service.interface_.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/password")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    /**
     * Request password reset - Send email with reset token
     * PUBLIC endpoint - no authentication required
     */
    @PostMapping("/forgot")
    public ApiResponse<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            passwordResetService.sendPasswordResetEmail(request.getEmail());
            return ApiResponse.success(
                "Password reset email sent! Please check your inbox (or spam/junk folder). Look for an email from TMS Tourism with subject '[TMS Tourism] Password Reset Request'.",
                "Password reset email sent successfully"
            );
        } catch (Exception e) {
            // Don't reveal if email exists or not for security reasons
            // But still return success message to user
            return ApiResponse.success(
                "If an account exists with this email, you will receive password reset instructions. Please check your spam folder if you don't see the email.",
                "Request processed"
            );
        }
    }

    /**
     * Reset password using token
     * PUBLIC endpoint - no authentication required
     */
    @PostMapping("/reset")
    public ApiResponse<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ApiResponse.success(
            "Password has been reset successfully. You can now login with your new password.",
            "Password reset successful"
        );
    }

    /**
     * Validate reset token
     * PUBLIC endpoint - Check if token is valid before showing reset form
     */
    @GetMapping("/validate-token")
    public ApiResponse<Boolean> validateToken(@RequestParam String token) {
        boolean isValid = passwordResetService.validateToken(token);
        if (isValid) {
            return ApiResponse.success("Token is valid", true);
        } else {
            return ApiResponse.error("Token is invalid or expired", false);
        }
    }
}
