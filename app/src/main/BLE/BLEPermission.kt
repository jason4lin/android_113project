package com.example.a113project

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class PermissionHelper(private val activity: Activity) {

    fun hasPermission(permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(activity, permissionType) ==
                PackageManager.PERMISSION_GRANTED
    }

    fun requestPermission(permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)
    }

    fun hasRequiredRuntimePermissions(): Boolean {
        // Implement your logic for checking required permissions
        return true
    }

    // Add other permission-related methods as needed

}
