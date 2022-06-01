package com.forceless.actionclock

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionRequest {
    companion object {
        fun getPermission(act: Activity, permissionCode: Array<String>) {
            for(i in permissionCode){
                if (ActivityCompat.checkSelfPermission(act,i)!=PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(
                        act,
                        permissionCode, 123
                    )
                }
            }

        }
    }
}