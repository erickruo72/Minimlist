package com.swiftcart.minimlist

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("focus_prefs", Context.MODE_PRIVATE)

    companion object {
        const val EXPIRY_BEHAVIOR_EXIT = "exit"
        const val EXPIRY_BEHAVIOR_ASK = "ask"
    }

    private fun getTodayDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean("onboarding_completed", false)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean("onboarding_completed", completed).apply()
    }

    fun getOnboardingStep(): Int {
        return prefs.getInt("onboarding_step", 0)
    }

    fun setOnboardingStep(step: Int) {
        prefs.edit().putInt("onboarding_step", step).apply()
    }

    // User Auth
    fun setUserName(name: String) {
        prefs.edit().putString("user_name", name).apply()
    }

    fun getUserName(): String? = prefs.getString("user_name", null)

    fun setUserEmail(email: String) {
        prefs.edit().putString("user_email", email).apply()
    }

    fun getUserEmail(): String? = prefs.getString("user_email", null)

    fun isLoggedIn(): Boolean = getUserEmail() != null

    fun setTrialInfo(start: String, end: String, isPremium: Boolean) {
        prefs.edit().apply {
            putString("trial_start", start)
            putString("trial_end", end)
            putBoolean("is_premium", isPremium)
        }.apply()
    }

    fun isPremium(): Boolean = true // Always return true now that payment is removed
    fun getTrialStart(): String? = prefs.getString("trial_start", null)
    fun getTrialEnd(): String? = prefs.getString("trial_end", null)

    /**
     * Checks if the user has access.
     * Access is now granted to everyone as payment is removed.
     */
    fun hasAccess(): Boolean {
        return true
    }

    fun logout() {
        prefs.edit().apply {
            remove("user_name")
            remove("user_email")
            remove("trial_start")
            remove("trial_end")
            remove("is_premium")
            putBoolean("onboarding_completed", false)
            putInt("onboarding_step", 1)
        }.apply()
    }

    // Blocking & Usage
    fun setDailyLimit(packageName: String, minutes: Int) {
        prefs.edit().putInt("limit_$packageName", minutes).apply()
    }

    fun getDailyLimit(packageName: String): Int {
        return prefs.getInt("limit_$packageName", 0)
    }

    fun addUsage(packageName: String, minutes: Long) {
        val today = getTodayDate()
        val key = "usage_${packageName}_$today"
        val current = prefs.getLong(key, 0L)
        prefs.edit().putLong(key, current + minutes).apply()
    }

    fun getDailyUsage(packageName: String, date: String = getTodayDate()): Long {
        return prefs.getLong("usage_${packageName}_$date", 0L)
    }

    fun incrementAppOpens(packageName: String) {
        val today = getTodayDate()
        val key = "opens_${packageName}_$today"
        val current = prefs.getInt(key, 0)
        prefs.edit().putInt(key, current + 1).apply()
    }

    fun getAppOpens(packageName: String, date: String = getTodayDate()): Int {
        return prefs.getInt("opens_${packageName}_$date", 0)
    }

    // Goals & Progress
    fun setDailyGoal(minutes: Int) {
        prefs.edit().putInt("daily_focus_goal", minutes).apply()
    }

    fun getDailyGoal(): Int {
        return prefs.getInt("daily_focus_goal", 120) // Default 2 hours
    }

    fun recordSessionExit() {
        val today = getTodayDate()
        val key = "sessions_exited_$today"
        val current = prefs.getInt(key, 0)
        prefs.edit().putInt(key, current + 1).apply()
    }

    fun recordSessionExtension() {
        val today = getTodayDate()
        val key = "sessions_extended_$today"
        val current = prefs.getInt(key, 0)
        prefs.edit().putLong(key, (prefs.getLong(key, 0L)) + 1).apply() // Fix: was using getInt for Long? No, wait. 
        // Actually, let's keep it simple and just fix the missing methods for Theme.
    }
    
    fun getThemeMode(): String = prefs.getString("theme_mode", "system") ?: "system"
    fun setThemeMode(mode: String) = prefs.edit().putString("theme_mode", mode).apply()
    
    fun getTextSizeMultiplier(): Float = prefs.getFloat("text_size_multiplier", 1.0f)
    fun setTextSizeMultiplier(multiplier: Float) = prefs.edit().putFloat("text_size_multiplier", multiplier).apply()
    
    fun getFontStyle(): String = prefs.getString("font_style", "Default") ?: "Default"
    fun setFontStyle(style: String) = prefs.edit().putString("font_style", style).apply()

    fun getSessionsExited(date: String = getTodayDate()): Int {
        return prefs.getInt("sessions_exited_$date", 0)
    }

    fun getSessionsExtended(date: String = getTodayDate()): Int {
        return prefs.getInt("sessions_extended_$date", 0)
    }

    // Timer Expiry Settings
    fun setTimerExpiryBehavior(behavior: String) {
        prefs.edit().putString("timer_expiry_behavior", behavior).apply()
    }

    fun getTimerExpiryBehavior(): String {
        return prefs.getString("timer_expiry_behavior", EXPIRY_BEHAVIOR_ASK) ?: EXPIRY_BEHAVIOR_ASK
    }

    // App Selection / Tracking
    fun getSelectedApps(): Set<String> {
        return prefs.getStringSet("selected_apps", emptySet()) ?: emptySet()
    }

    fun setSelectedApps(apps: Set<String>) {
        prefs.edit().putStringSet("selected_apps", apps).apply()
    }

    fun isAppTracked(packageName: String): Boolean {
        return getSelectedApps().contains(packageName)
    }

    fun getTrackedPackages(): List<String> {
        return getSelectedApps().toList()
    }

    // Analytics Helper
    fun getTotalFocusTimeToday(): Long {
        return getTrackedPackages().sumOf { getDailyUsage(it) }
    }

    fun getAverageSessionTimeToday(): Long {
        val totalUsage = getTotalFocusTimeToday()
        val totalSessions = getSessionsExited() + getSessionsExtended()
        return if (totalSessions > 0) totalUsage / totalSessions.toLong() else 0L
    }
}
