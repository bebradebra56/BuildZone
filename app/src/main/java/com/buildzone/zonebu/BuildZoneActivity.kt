package com.buildzone.zonebu

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.buildzone.zonebu.grv.BuildZoneGlobalLayoutUtil
import com.buildzone.zonebu.grv.buildZoneSetupSystemBars
import com.buildzone.zonebu.grv.presentation.app.BuildZoneApplication
import com.buildzone.zonebu.grv.presentation.pushhandler.BuildZonePushHandler
import org.koin.android.ext.android.inject

class BuildZoneActivity : AppCompatActivity() {

    private val buildZonePushHandler by inject<BuildZonePushHandler>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        buildZoneSetupSystemBars()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_build_zone)

        val buildZoneRootView = findViewById<View>(android.R.id.content)
        BuildZoneGlobalLayoutUtil().buildZoneAssistActivity(this)
        ViewCompat.setOnApplyWindowInsetsListener(buildZoneRootView) { buildZoneView, buildZoneInsets ->
            val buildZoneSystemBars = buildZoneInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val buildZoneDisplayCutout = buildZoneInsets.getInsets(WindowInsetsCompat.Type.displayCutout())
            val buildZoneIme = buildZoneInsets.getInsets(WindowInsetsCompat.Type.ime())


            val buildZoneTopPadding = maxOf(buildZoneSystemBars.top, buildZoneDisplayCutout.top)
            val buildZoneLeftPadding = maxOf(buildZoneSystemBars.left, buildZoneDisplayCutout.left)
            val buildZoneRightPadding = maxOf(buildZoneSystemBars.right, buildZoneDisplayCutout.right)
            window.setSoftInputMode(BuildZoneApplication.buildZoneInputMode)

            if (window.attributes.softInputMode == WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN) {
                Log.d(BuildZoneApplication.BUILD_ZONE_MAIN_TAG, "ADJUST PUN")
                val buildZoneBottomInset = maxOf(buildZoneSystemBars.bottom, buildZoneDisplayCutout.bottom)

                buildZoneView.setPadding(buildZoneLeftPadding, buildZoneTopPadding, buildZoneRightPadding, 0)

                buildZoneView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = buildZoneBottomInset
                }
            } else {
                Log.d(BuildZoneApplication.BUILD_ZONE_MAIN_TAG, "ADJUST RESIZE")

                val buildZoneBottomInset = maxOf(buildZoneSystemBars.bottom, buildZoneDisplayCutout.bottom, buildZoneIme.bottom)

                buildZoneView.setPadding(buildZoneLeftPadding, buildZoneTopPadding, buildZoneRightPadding, 0)

                buildZoneView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = buildZoneBottomInset
                }
            }



            WindowInsetsCompat.CONSUMED
        }
        Log.d(BuildZoneApplication.BUILD_ZONE_MAIN_TAG, "Activity onCreate()")
        buildZonePushHandler.buildZoneHandlePush(intent.extras)
    }
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            buildZoneSetupSystemBars()
        }
    }

    override fun onResume() {
        super.onResume()
        buildZoneSetupSystemBars()
    }
}