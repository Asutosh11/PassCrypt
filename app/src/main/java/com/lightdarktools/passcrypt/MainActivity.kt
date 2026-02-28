package com.lightdarktools.passcrypt

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhonelinkSetup
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.core.view.WindowCompat
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.lightdarktools.passcrypt.data.local.AppDatabase
import com.lightdarktools.passcrypt.data.local.SettingsManager
import com.lightdarktools.passcrypt.data.repository.PasswordRepositoryImpl
import com.lightdarktools.passcrypt.domain.usecase.*
import com.lightdarktools.passcrypt.presentation.viewmodel.PasswordViewModel
import com.lightdarktools.passcrypt.presentation.viewmodel.PasswordViewModelFactory
import com.lightdarktools.passcrypt.presentation.ui.components.RatingDialog
import com.lightdarktools.passcrypt.presentation.ui.components.TutorialOverlay
import com.lightdarktools.passcrypt.presentation.ui.fragments.ExportFragment
import com.lightdarktools.passcrypt.presentation.ui.fragments.VaultFragment
import com.lightdarktools.passcrypt.presentation.ui.theme.PassCryptTheme
import java.util.concurrent.Executor

class MainActivity : FragmentActivity() {
    private lateinit var viewModel: PasswordViewModel
    private var isUserAuthenticated by mutableStateOf(false)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        com.tom_roush.pdfbox.android.PDFBoxResourceLoader.init(applicationContext)
        super.onCreate(savedInstanceState)
        
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = PasswordRepositoryImpl(database.passwordDao())
        val settingsManager = SettingsManager(applicationContext)
        
        // Initialize Use Cases
        val useCases = PasswordUseCases(
            getPasswords = GetPasswordsUseCase(repository),
            addPassword = AddPasswordUseCase(repository),
            deletePassword = DeletePasswordUseCase(repository),
            updatePassword = UpdatePasswordUseCase(repository),
            deleteAllPasswords = DeleteAllPasswordsUseCase(repository),
            getCategories = GetCategoriesUseCase(repository),
            addPasswords = AddPasswordsUseCase(repository)
        )
        
        val factory = PasswordViewModelFactory(useCases, settingsManager)
        viewModel = ViewModelProvider(this, factory)[PasswordViewModel::class.java]
        
        enableEdgeToEdge()
        showBiometricPrompt()

        // Increment open count exactly once per app session
        lifecycleScope.launch {
            viewModel.incrementAppOpenCount()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiEvent.collect { event ->
                    when (event) {
                        is PasswordViewModel.UiEvent.ShowToast ->
                            Toast.makeText(this@MainActivity, event.message, Toast.LENGTH_LONG).show()
                        is PasswordViewModel.UiEvent.ShowStringResourceToast ->
                            Toast.makeText(this@MainActivity, getString(event.resId, *event.args), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
        setContent {
            PassCryptTheme {
                if (isUserAuthenticated) {
                    MainAppLayout()
                } else {
                    LockedScreen(onRetry = { showBiometricPrompt() })
                }
            }
        }
    }

    @Composable
    fun MainAppLayout() {
        val selectedItem by viewModel.currentTab.collectAsState()
        val hasSeenTutorial by viewModel.hasSeenTutorial.collectAsState()
        val showRatingDialog by viewModel.showRatingDialog.collectAsState()
        val context = LocalContext.current
        val items = listOf(stringResource(R.string.tab_vault), stringResource(R.string.tab_transfer))
        val icons: List<ImageVector> = listOf(Icons.Default.Lock, Icons.Default.ImportExport) 
        val playStoreUrl = stringResource(R.string.play_store_url)

        // Stable ID for the fragment container
        val containerId = remember { View.generateViewId() }

        // Show rating dialog 2s after authentication succeeds (if criteria met)
        LaunchedEffect(Unit) {
            delay(2000L)
            viewModel.checkAndTriggerRatingDialog()
        }

        // Trigger fragment transactions when selectedItem changes
        LaunchedEffect(selectedItem) {
            val fragment = if (selectedItem == 0) VaultFragment() else ExportFragment()
            supportFragmentManager.beginTransaction()
                .replace(containerId, fragment)
                .commit()
        }

        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    items.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = { Icon(icons[index], contentDescription = item) },
                            label = { Text(item, fontWeight = FontWeight.Bold) },
                            selected = selectedItem == index,
                            onClick = { viewModel.setTab(index) }
                        )
                    }
                }
            }
        ) { paddingValues ->
            AndroidView(
                factory = { context ->
                    FragmentContainerView(context).apply {
                        id = containerId
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding())
            )
        }

        // Show tutorial on first run
        if (!hasSeenTutorial) {
            TutorialOverlay(onDismiss = { viewModel.markTutorialSeen() })
        }

        // Show rating dialog when triggered
        if (showRatingDialog) {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(playStoreUrl))
            RatingDialog(
                onRateClick = {
                    context.startActivity(intent)
                    viewModel.setHasRatedApp(true)
                },
                onAlreadyRatedClick = {
                    viewModel.setHasRatedApp(true)
                },
                onDismiss = {
                    viewModel.dismissRatingDialog()
                }
            )
        }
    }

    private fun showBiometricPrompt() {
        val executor: Executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    isUserAuthenticated = true
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }
            })

        val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.biometric_title))
            .setSubtitle(getString(R.string.biometric_subtitle))

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            promptInfoBuilder.setAllowedAuthenticators(
                androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or 
                androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
        } else {
            promptInfoBuilder.setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG)
            promptInfoBuilder.setNegativeButtonText(getString(R.string.biometric_negative))
        }

        biometricPrompt.authenticate(promptInfoBuilder.build())
    }
}

@Composable
fun LockedScreen(onRetry: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Branded status bar area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsTopHeight(WindowInsets.statusBars)
                    .background(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else primary)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            // M3 tonal icon circle
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.size(96.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(52.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = stringResource(R.string.locked_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.locked_description),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp).padding(top = 2.dp)
                        )
                        Text(
                            text = stringResource(R.string.locked_offline_badge),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.locked_security_details),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(stringResource(R.string.btn_unlock_vault))
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    val intent = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                        Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                            putExtra(
                                Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or
                                androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
                            )
                        }
                    } else {
                        Intent(Settings.ACTION_SECURITY_SETTINGS)
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(0.8f),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.PhonelinkSetup, contentDescription = null,
                    modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.btn_setup_lock))
            }
        }
    }
}}