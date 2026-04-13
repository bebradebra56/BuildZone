package com.buildzone.zonebu.grv.presentation.ui.load

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.buildzone.zonebu.MainActivity
import com.buildzone.zonebu.R
import com.buildzone.zonebu.databinding.FragmentLoadBuildZoneBinding
import com.buildzone.zonebu.grv.data.shar.BuildZoneSharedPreference
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class BuildZoneLoadFragment : Fragment(R.layout.fragment_load_build_zone) {
    private lateinit var buildZoneLoadBinding: FragmentLoadBuildZoneBinding

    private val buildZoneLoadViewModel by viewModel<BuildZoneLoadViewModel>()

    private val buildZoneSharedPreference by inject<BuildZoneSharedPreference>()

    private var buildZoneUrl = ""

    private val buildZoneRequestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        buildZoneSharedPreference.buildZoneNotificationState = 2
        buildZoneNavigateToSuccess(buildZoneUrl)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buildZoneLoadBinding = FragmentLoadBuildZoneBinding.bind(view)

        buildZoneLoadBinding.buildZoneGrandButton.setOnClickListener {
            val buildZonePermission = Manifest.permission.POST_NOTIFICATIONS
            buildZoneRequestNotificationPermission.launch(buildZonePermission)
        }

        buildZoneLoadBinding.buildZoneSkipButton.setOnClickListener {
            buildZoneSharedPreference.buildZoneNotificationState = 1
            buildZoneSharedPreference.buildZoneNotificationRequest =
                (System.currentTimeMillis() / 1000) + 259200
            buildZoneNavigateToSuccess(buildZoneUrl)
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                buildZoneLoadViewModel.buildZoneHomeScreenState.collect {
                    when (it) {
                        is BuildZoneLoadViewModel.BuildZoneHomeScreenState.BuildZoneLoading -> {

                        }

                        is BuildZoneLoadViewModel.BuildZoneHomeScreenState.BuildZoneError -> {
                            requireActivity().startActivity(
                                Intent(
                                    requireContext(),
                                    MainActivity::class.java
                                )
                            )
                            requireActivity().finish()
                        }

                        is BuildZoneLoadViewModel.BuildZoneHomeScreenState.BuildZoneSuccess -> {
                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
                                val buildZoneNotificationState = buildZoneSharedPreference.buildZoneNotificationState
                                when (buildZoneNotificationState) {
                                    0 -> {
                                        buildZoneLoadBinding.buildZoneNotiGroup.visibility = View.VISIBLE
                                        buildZoneLoadBinding.buildZoneLoadingGroup.visibility = View.GONE
                                        buildZoneUrl = it.data
                                    }
                                    1 -> {
                                        if (System.currentTimeMillis() / 1000 > buildZoneSharedPreference.buildZoneNotificationRequest) {
                                            buildZoneLoadBinding.buildZoneNotiGroup.visibility = View.VISIBLE
                                            buildZoneLoadBinding.buildZoneLoadingGroup.visibility = View.GONE
                                            buildZoneUrl = it.data
                                        } else {
                                            buildZoneNavigateToSuccess(it.data)
                                        }
                                    }
                                    2 -> {
                                        buildZoneNavigateToSuccess(it.data)
                                    }
                                }
                            } else {
                                buildZoneNavigateToSuccess(it.data)
                            }
                        }

                        BuildZoneLoadViewModel.BuildZoneHomeScreenState.BuildZoneNotInternet -> {
                            buildZoneLoadBinding.buildZoneStateGroup.visibility = View.VISIBLE
                            buildZoneLoadBinding.buildZoneLoadingGroup.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }


    private fun buildZoneNavigateToSuccess(data: String) {
        findNavController().navigate(
            R.id.action_buildZoneLoadFragment_to_buildZoneV,
            bundleOf(BUILD_ZONE_D to data)
        )
    }

    companion object {
        const val BUILD_ZONE_D = "buildZoneData"
    }
}