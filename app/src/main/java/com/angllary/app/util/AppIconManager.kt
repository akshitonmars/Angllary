package com.angllary.app.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

object AppIconManager {
    fun syncIconWithTheme(context: Context, themeMode: String, isSystemDark: Boolean) {
        val isLight = when (themeMode) {
            "light" -> true
            "dark" -> false
            else -> !isSystemDark
        }
        applyIcon(context, isLight)
    }

    private fun applyIcon(context: Context, isLight: Boolean) {
        val pm = context.packageManager
        val packageName = context.packageName
        val darkComponent = ComponentName(packageName, "$packageName.MainActivity")
        val lightComponent = ComponentName(packageName, "$packageName.MainActivityLight")

        val targetEnable = if (isLight) lightComponent else darkComponent
        val targetDisable = if (isLight) darkComponent else lightComponent

        runCatching {
            val currentState = pm.getComponentEnabledSetting(targetEnable)
            if (currentState != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                pm.setComponentEnabledSetting(
                    targetEnable,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )
                pm.setComponentEnabledSetting(
                    targetDisable,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
            }
        }
    }
}
