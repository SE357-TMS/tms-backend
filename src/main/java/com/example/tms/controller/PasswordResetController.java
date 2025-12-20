package com.example.tms.controller;

import com.example.tms.dto.request.ForgotPasswordRequest;
import com.example.tms.dto.request.ResetPasswordRequest;
import com.example.tms.dto.response.ApiResponse;
import com.example.tms.service.interface_.PasswordResetService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/password")
@RequiredArgsConstructor
@Tag(name = "Password Reset", description = "APIs for password reset functionality")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @Operation(
        summary = "Request password reset",
        description = "Send password reset email with token to user's email address. Public endpoint - no authentication required."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password reset email sent successfully")
    })
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

    @Operation(
        summary = "Reset password",
        description = "Reset user password using the token received via email. Public endpoint - no authentication required."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password reset successful"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
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

