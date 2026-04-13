package com.buildzone.zonebu.grv.presentation.ui.view

import android.annotation.SuppressLint
import android.widget.FrameLayout
import androidx.lifecycle.ViewModel

class BuildZoneDataStore : ViewModel(){
    val buildZoneViList: MutableList<BuildZoneVi> = mutableListOf()
    var buildZoneIsFirstCreate = true
    @SuppressLint("StaticFieldLeak")
    lateinit var buildZoneContainerView: FrameLayout
    @SuppressLint("StaticFieldLeak")
    lateinit var buildZoneView: BuildZoneVi

}