package com.buildzone.zonebu.grv.presentation.ui.view

import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.buildzone.zonebu.grv.presentation.app.BuildZoneApplication
import com.buildzone.zonebu.grv.presentation.ui.load.BuildZoneLoadFragment
import org.koin.android.ext.android.inject

class BuildZoneV : Fragment(){

    private lateinit var buildZonePhoto: Uri
    private var buildZoneFilePathFromChrome: ValueCallback<Array<Uri>>? = null

    private val buildZoneTakeFile: ActivityResultLauncher<PickVisualMediaRequest> = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        buildZoneFilePathFromChrome?.onReceiveValue(arrayOf(it ?: Uri.EMPTY))
        buildZoneFilePathFromChrome = null
    }

    private val buildZoneTakePhoto: ActivityResultLauncher<Uri> = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it) {
            buildZoneFilePathFromChrome?.onReceiveValue(arrayOf(buildZonePhoto))
            buildZoneFilePathFromChrome = null
        } else {
            buildZoneFilePathFromChrome?.onReceiveValue(null)
            buildZoneFilePathFromChrome = null
        }
    }

    private val buildZoneDataStore by activityViewModels<BuildZoneDataStore>()


    private val buildZoneViFun by inject<BuildZoneViFun>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(BuildZoneApplication.BUILD_ZONE_MAIN_TAG, "Fragment onCreate")
        CookieManager.getInstance().setAcceptCookie(true)
        requireActivity().onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (buildZoneDataStore.buildZoneView.canGoBack()) {
                        buildZoneDataStore.buildZoneView.goBack()
                        Log.d(BuildZoneApplication.BUILD_ZONE_MAIN_TAG, "WebView can go back")
                    } else if (buildZoneDataStore.buildZoneViList.size > 1) {
                        Log.d(BuildZoneApplication.BUILD_ZONE_MAIN_TAG, "WebView can`t go back")
                        buildZoneDataStore.buildZoneViList.removeAt(buildZoneDataStore.buildZoneViList.lastIndex)
                        Log.d(BuildZoneApplication.BUILD_ZONE_MAIN_TAG, "WebView list size ${buildZoneDataStore.buildZoneViList.size}")
                        buildZoneDataStore.buildZoneView.destroy()
                        val previousWebView = buildZoneDataStore.buildZoneViList.last()
                        buildZoneAttachWebViewToContainer(previousWebView)
                        buildZoneDataStore.buildZoneView = previousWebView
                    }
                }

            })
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (buildZoneDataStore.buildZoneIsFirstCreate) {
            buildZoneDataStore.buildZoneIsFirstCreate = false
            buildZoneDataStore.buildZoneContainerView = FrameLayout(requireContext()).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                id = View.generateViewId()
            }
            return buildZoneDataStore.buildZoneContainerView
        } else {
            return buildZoneDataStore.buildZoneContainerView
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(BuildZoneApplication.BUILD_ZONE_MAIN_TAG, "onViewCreated")
        if (buildZoneDataStore.buildZoneViList.isEmpty()) {
            buildZoneDataStore.buildZoneView = BuildZoneVi(requireContext(), object :
                BuildZoneCallBack {
                override fun buildZoneHandleCreateWebWindowRequest(buildZoneVi: BuildZoneVi) {
                    buildZoneDataStore.buildZoneViList.add(buildZoneVi)
                    Log.d(BuildZoneApplication.BUILD_ZONE_MAIN_TAG, "WebView list size = ${buildZoneDataStore.buildZoneViList.size}")
                    Log.d(BuildZoneApplication.BUILD_ZONE_MAIN_TAG, "CreateWebWindowRequest")
                    buildZoneDataStore.buildZoneView = buildZoneVi
                    buildZoneVi.buildZoneSetFileChooserHandler { callback ->
                        buildZoneHandleFileChooser(callback)
                    }
                    buildZoneAttachWebViewToContainer(buildZoneVi)
                }

            }, buildZoneWindow = requireActivity().window).apply {
                buildZoneSetFileChooserHandler { callback ->
                    buildZoneHandleFileChooser(callback)
                }
            }
            buildZoneDataStore.buildZoneView.buildZoneFLoad(arguments?.getString(
                BuildZoneLoadFragment.BUILD_ZONE_D) ?: "")
//            ejvview.fLoad("www.google.com")
            buildZoneDataStore.buildZoneViList.add(buildZoneDataStore.buildZoneView)
            buildZoneAttachWebViewToContainer(buildZoneDataStore.buildZoneView)
        } else {
            buildZoneDataStore.buildZoneViList.forEach { webView ->
                webView.buildZoneSetFileChooserHandler { callback ->
                    buildZoneHandleFileChooser(callback)
                }
            }
            buildZoneDataStore.buildZoneView = buildZoneDataStore.buildZoneViList.last()

            buildZoneAttachWebViewToContainer(buildZoneDataStore.buildZoneView)
        }
        Log.d(BuildZoneApplication.BUILD_ZONE_MAIN_TAG, "WebView list size = ${buildZoneDataStore.buildZoneViList.size}")
    }

    private fun buildZoneHandleFileChooser(callback: ValueCallback<Array<Uri>>?) {
        Log.d(BuildZoneApplication.BUILD_ZONE_MAIN_TAG, "handleFileChooser called, callback: ${callback != null}")

        buildZoneFilePathFromChrome = callback

        val listItems: Array<out String> = arrayOf("Select from file", "To make a photo")
        val listener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                0 -> {
                    Log.d(BuildZoneApplication.BUILD_ZONE_MAIN_TAG, "Launching file picker")
                    buildZoneTakeFile.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
                1 -> {
                    Log.d(BuildZoneApplication.BUILD_ZONE_MAIN_TAG, "Launching camera")
                    buildZonePhoto = buildZoneViFun.buildZoneSavePhoto()
                    buildZoneTakePhoto.launch(buildZonePhoto)
                }
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Choose a method")
            .setItems(listItems, listener)
            .setCancelable(true)
            .setOnCancelListener {
                Log.d(BuildZoneApplication.BUILD_ZONE_MAIN_TAG, "File chooser canceled")
                callback?.onReceiveValue(null)
                buildZoneFilePathFromChrome = null
            }
            .create()
            .show()
    }

    private fun buildZoneAttachWebViewToContainer(w: BuildZoneVi) {
        buildZoneDataStore.buildZoneContainerView.post {
            (w.parent as? ViewGroup)?.removeView(w)
            buildZoneDataStore.buildZoneContainerView.removeAllViews()
            buildZoneDataStore.buildZoneContainerView.addView(w)
        }
    }


}