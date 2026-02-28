package com.lightdarktools.passcrypt.presentation.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lightdarktools.passcrypt.R
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.lightdarktools.passcrypt.presentation.ui.MainScreen
import com.lightdarktools.passcrypt.presentation.viewmodel.PasswordViewModel
import com.lightdarktools.passcrypt.presentation.ui.theme.PassCryptTheme

class VaultFragment : Fragment() {
    private val viewModel: PasswordViewModel by activityViewModels()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val windowSizeClass = calculateWindowSizeClass(requireActivity())
                PassCryptTheme {
                    MainScreen(
                        viewModel = viewModel,
                        windowWidthSizeClass = windowSizeClass.widthSizeClass
                    )
                }
            }
        }
    }
}
