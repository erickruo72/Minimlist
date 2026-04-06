package com.swiftcart.minimlist

import android.graphics.drawable.Drawable

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable? = null
)

data class AppLimit(
    val packageName: String,
    val dailyLimitMinutes: Int = 0,
    val currentUsageMinutes: Long = 0,
    val lastUsedTimestamp: Long = 0
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val device_id: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class VerifyEmailRequest(
    val email: String,
    val code: String
)

data class AuthResponse(
    val status: String,
    val message: String,
    val name: String? = null,
    val email: String? = null,
    val trial_start: String? = null,
    val trial_end: String? = null,
    val is_premium: Boolean? = null
)

data class VerifyEmailResponse(
    val status: String,
    val message: String,
    val trial_end: String? = null
)
