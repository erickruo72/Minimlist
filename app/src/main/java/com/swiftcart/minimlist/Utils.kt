package com.swiftcart.minimlist

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.provider.Settings
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

fun getInstalledApps(context: Context): List<AppInfo> {
    val pm = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
    return pm.queryIntentActivities(intent, 0)
        .map { AppInfo(name = it.loadLabel(pm).toString(), packageName = it.activityInfo.packageName) }
        .filter { it.packageName != context.packageName }
        .sortedBy { it.name }
}

fun launchAppWithTimer(context: Context, app: AppInfo, minutes: Int) {
    context.startService(Intent(context, FocusAccessibilityService::class.java).apply { 
        action = FocusAccessibilityService.ACTION_START_TIMER
        putExtra(FocusAccessibilityService.EXTRA_PACKAGE_NAME, app.packageName)
        putExtra(FocusAccessibilityService.EXTRA_DURATION_MINUTES, minutes) 
    })
    val launch = context.packageManager.getLaunchIntentForPackage(app.packageName)
    if (launch != null) {
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launch)
    }
}

fun checkUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    return appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName) == AppOpsManager.MODE_ALLOWED
}

fun isAccessibilityServiceEnabled(context: Context): Boolean = 
    Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)?.contains(context.packageName) == true

fun isDefaultLauncher(context: Context): Boolean {
    val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_HOME) }
    val resolveInfo = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
    return resolveInfo?.activityInfo?.packageName == context.packageName
}

/**
 * Checks if the device has an active network connection AND actual internet access.
 * This prevents the "connected but no internet" issue.
 */
fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    
    // Check if the network has the INTERNET capability and is VALIDATED by the system
    val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    val isValidated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    
    return hasInternet && isValidated
}

/**
 * A more aggressive check that attempts to ping a reliable server.
 * Useful for the "Retry" button to ensure connectivity is truly restored.
 */
fun hasRealInternetAccess(): Boolean {
    return try {
        val timeoutMs = 1500
        val socket = Socket()
        val socketAddress = InetSocketAddress("8.8.8.8", 53)
        socket.connect(socketAddress, timeoutMs)
        socket.close()
        true
    } catch (e: IOException) {
        false
    }
}
