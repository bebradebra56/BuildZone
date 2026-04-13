package com.buildzone.zonebu.grv

import android.app.Activity
import android.graphics.Rect
import android.view.View
import android.widget.FrameLayout
import com.buildzone.zonebu.grv.presentation.app.BuildZoneApplication

class BuildZoneGlobalLayoutUtil {

    private var buildZoneMChildOfContent: View? = null
    private var buildZoneUsableHeightPrevious = 0

    fun buildZoneAssistActivity(activity: Activity) {
        val content = activity.findViewById<FrameLayout>(android.R.id.content)
        buildZoneMChildOfContent = content.getChildAt(0)

        buildZoneMChildOfContent?.viewTreeObserver?.addOnGlobalLayoutListener {
            possiblyResizeChildOfContent(activity)
        }
    }

    private fun possiblyResizeChildOfContent(activity: Activity) {
        val buildZoneUsableHeightNow = buildZoneComputeUsableHeight()
        if (buildZoneUsableHeightNow != buildZoneUsableHeightPrevious) {
            val buildZoneUsableHeightSansKeyboard = buildZoneMChildOfContent?.rootView?.height ?: 0
            val buildZoneHeightDifference = buildZoneUsableHeightSansKeyboard - buildZoneUsableHeightNow

            if (buildZoneHeightDifference > (buildZoneUsableHeightSansKeyboard / 4)) {
                activity.window.setSoftInputMode(BuildZoneApplication.buildZoneInputMode)
            } else {
                activity.window.setSoftInputMode(BuildZoneApplication.buildZoneInputMode)
            }
//            mChildOfContent?.requestLayout()
            buildZoneUsableHeightPrevious = buildZoneUsableHeightNow
        }
    }

    private fun buildZoneComputeUsableHeight(): Int {
        val r = Rect()
        buildZoneMChildOfContent?.getWindowVisibleDisplayFrame(r)
        return r.bottom - r.top  // Visible height без status bar
    }
}