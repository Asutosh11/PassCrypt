package com.lightdarktools.passcrypt.presentation.ui.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.lightdarktools.passcrypt.R
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.FileProvider
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_AZTEC
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_QR_CODE
import com.google.mlkit.vision.common.InputImage
import com.lightdarktools.passcrypt.presentation.viewmodel.PasswordViewModel
import com.lightdarktools.passcrypt.presentation.ui.components.*
import com.lightdarktools.passcrypt.presentation.ui.theme.PassCryptTheme
import com.lightdarktools.passcrypt.common.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.Inet4Address
import java.net.NetworkInterface

class ExportFragment : Fragment() {
    private val viewModel: PasswordViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setContent {
            PassCryptTheme {
                ExportScreen(
                    viewModel = viewModel,
                    onSuccessfulImport = {
                        viewModel.clearImportState()
                        viewModel.setTab(0)
                    }
                )
            }
        }
    }
}

private enum class TransferMode { PICKER, QR_SEND, QR_RECEIVE, FILE_SEND, FILE_RECEIVE, CSV_EXPORT }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(viewModel: PasswordViewModel, onSuccessfulImport: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var mode by remember { mutableStateOf(TransferMode.PICKER) }
    var pinDialogFor by remember { mutableStateOf<TransferMode?>(null) }
    var cachedPin by remember { mutableStateOf("") }
    var fileContentToImport by remember { mutableStateOf<String?>(null) }
    var pdfBytesToImport by remember { mutableStateOf<ByteArray?>(null) }
    var pdfBytesToSave by remember { mutableStateOf<ByteArray?>(null) }
    var csvContentToSave by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf<String?>(null) }
    var qrChunks by remember { mutableStateOf<List<String>>(emptyList()) }
    val importError by viewModel.importError.collectAsState()
    val isTransferIntroExpanded by viewModel.isTransferIntroExpanded.collectAsState()


    val cameraPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) mode = TransferMode.QR_RECEIVE
        else viewModel.showStringResourceToast(R.string.camera_permission_required)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                        navigationIcon = {
                            if (mode != TransferMode.PICKER) {
                                IconButton(onClick = {
                                    viewModel.clearImportState()
                                    qrChunks = emptyList()
                                    mode = TransferMode.PICKER
                                }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.btn_back), tint = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary)
                                }
                            }
                        },
                        title = {
                            Text(
                                when (mode) {
                                    TransferMode.PICKER -> stringResource(R.string.mode_vault_transfer)
                                    TransferMode.QR_SEND -> stringResource(R.string.mode_qr_send)
                                    TransferMode.QR_RECEIVE -> stringResource(R.string.mode_qr_receive)
                                    TransferMode.FILE_SEND -> stringResource(R.string.mode_file_export)
                                    TransferMode.FILE_RECEIVE -> stringResource(R.string.mode_file_import)
                                    TransferMode.CSV_EXPORT -> stringResource(R.string.mode_csv_export)
                                },
                                fontWeight = FontWeight.Bold
                            )
                        },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.primary,
                    titleContentColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        // Launcher for picking an exported backup file
    val pdfSaveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    pdfBytesToSave?.let { bytes ->
                        context.contentResolver.openOutputStream(it)?.use { os ->
                            os.write(bytes)
                        }
                        viewModel.showStringResourceToast(R.string.backup_saved_success)
                    }
                } catch (e: Exception) {
                    viewModel.showStringResourceToast(R.string.save_failed, e.message ?: "")
                } finally {
                    pdfBytesToSave = null
                }
            }
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    val mimeType = context.contentResolver.getType(it)
                    if (mimeType == "application/pdf" || it.path?.endsWith(".pdf", ignoreCase = true) == true) {
                        val bytes = context.contentResolver.openInputStream(it)?.readBytes()
                        if (bytes != null) {
                            pdfBytesToImport = bytes
                            pinDialogFor = TransferMode.FILE_RECEIVE
                        }
                    } else {
                        val inputStream = context.contentResolver.openInputStream(it)
                        val content = inputStream?.bufferedReader()?.use { reader -> reader.readText() }
                        if (content != null) {
                            fileContentToImport = content
                            pinDialogFor = TransferMode.FILE_RECEIVE
                        }
                    }
                } catch (e: Exception) {
                    viewModel.showStringResourceToast(R.string.error_reading_file, e.message ?: "")
                }
            }
        }
    }

    val csvSaveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    csvContentToSave?.let { content ->
                        context.contentResolver.openOutputStream(it)?.use { os ->
                            os.write(content.toByteArray())
                        }
                        viewModel.showStringResourceToast(R.string.csv_export_success)
                    }
                } catch (e: Exception) {
                    viewModel.showStringResourceToast(R.string.save_failed, e.message ?: "")
                } finally {
                    csvContentToSave = null
                }
            }
        }
    }

    AnimatedContent(targetState = mode, label = "transfer_mode_transition") { currentMode ->
        when (currentMode) {
            TransferMode.PICKER -> MethodPickerScreen(
                onQrSend = { pinDialogFor = TransferMode.QR_SEND },
                onQrReceive = { pinDialogFor = TransferMode.QR_RECEIVE },
                onFileSend = { pinDialogFor = TransferMode.FILE_SEND },
                onFileReceive = { filePickerLauncher.launch("*/*") },
                isIntroExpanded = isTransferIntroExpanded,
                onToggleIntro = { viewModel.toggleTransferIntroExpanded() },
                padding = padding,
                onCsvExport = { mode = TransferMode.CSV_EXPORT }
            )
            TransferMode.QR_SEND -> QrSendScreen(chunks = qrChunks, padding = padding)
            TransferMode.QR_RECEIVE -> QrReceiveScreen(
                viewModel = viewModel,
                importError = importError,
                pin = cachedPin,
                onSuccess = { onSuccessfulImport(); mode = TransferMode.PICKER },
                padding = padding
            )
            TransferMode.CSV_EXPORT -> CsvExportScreen(
                onExport = { 
                    scope.launch {
                        csvContentToSave = viewModel.generateBackupCsv()
                        csvSaveLauncher.launch("offline_password_manager_ios_backup_${System.currentTimeMillis()}.csv")
                    }
                },
                padding = padding
            )
            else -> {}
        }
    }

    pinDialogFor?.let { purpose ->
        PinInputDialog(
            title = when (purpose) {
                TransferMode.QR_SEND -> stringResource(R.string.pin_export)
                TransferMode.QR_RECEIVE -> stringResource(R.string.pin_import)
                TransferMode.FILE_SEND -> stringResource(R.string.pin_export_file)
                TransferMode.FILE_RECEIVE -> stringResource(R.string.pin_import_file)
                TransferMode.CSV_EXPORT -> stringResource(R.string.pin_export_csv)
                else -> stringResource(R.string.pin_generic)
            },
            onDismiss = { pinDialogFor = null },
            onConfirm = { pin ->
                cachedPin = pin
                pinDialogFor = null
                when (purpose) {
                    TransferMode.QR_SEND -> {
                        scope.launch {
                            try {
                                qrChunks = viewModel.prepareExportChunks(pin)
                                mode = TransferMode.QR_SEND
                            } catch (e: Exception) {
                                viewModel.showStringResourceToast(R.string.save_failed, e.message ?: "")
                            }
                        }
                    }
                    TransferMode.QR_RECEIVE -> {
                        cameraPermLauncher.launch(android.Manifest.permission.CAMERA)
                    }
                    TransferMode.FILE_SEND -> {
                        scope.launch {
                            try {
                                val pdfData = viewModel.generateBackupPdf(pin)
                                pdfBytesToSave = pdfData
                                pdfSaveLauncher.launch("offline_password_manager_backup_${System.currentTimeMillis()}.pdf")
                            } catch (e: Exception) {
                                viewModel.showStringResourceToast(R.string.save_failed, e.message ?: "")
                            }
                        }
                    }
                    TransferMode.FILE_RECEIVE -> {
                        scope.launch {
                            try {
                                pdfBytesToImport?.let {
                                    viewModel.importFromPdf(it, pin)
                                    onSuccessfulImport()
                                }
                                fileContentToImport?.let {
                                    viewModel.importFromEncryptedString(it, pin)
                                    onSuccessfulImport()
                                }
                            } catch (e: Exception) {
                                viewModel.showToast(e.message ?: context.getString(R.string.error_wrong_pin_file))
                            } finally {
                                fileContentToImport = null
                                pdfBytesToImport = null
                            }
                        }
                    }
                    else -> {}
                }
            }
        )
    }
}
}

@Composable
private fun MethodPickerScreen(
    onQrSend: () -> Unit,
    onQrReceive: () -> Unit,
    onFileSend: () -> Unit,
    onFileReceive: () -> Unit,
    onCsvExport: () -> Unit,
    isIntroExpanded: Boolean,
    onToggleIntro: () -> Unit,
    padding: PaddingValues
) {
    val primary = MaterialTheme.colorScheme.primary

    LazyColumn(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent
            ) {
                Text(
                    text = stringResource(R.string.transfer_select_method),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else Color.Black
                )
            }
        }

        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TransferMethodCard(
                    illustration = { QrIllustration() },
                    title = stringResource(R.string.transfer_qr_title),
                    speed = stringResource(R.string.transfer_qr_speed),
                    description = stringResource(R.string.transfer_qr_desc),
                    onSend = onQrSend,
                    onReceive = onQrReceive
                )

                TransferMethodCard(
                    illustration = { FileIllustration() },
                    title = stringResource(R.string.transfer_file_title),
                    speed = stringResource(R.string.transfer_file_speed),
                    description = stringResource(R.string.transfer_file_desc),
                    onSend = onFileSend,
                    onReceive = onFileReceive
                )

                TransferMethodCard(
                    illustration = { AppleIllustration() },
                    title = stringResource(R.string.transfer_ios_title),
                    speed = stringResource(R.string.transfer_ios_speed),
                    description = stringResource(R.string.transfer_ios_desc),
                    onSend = onCsvExport,
                    onReceive = null // Receiving CSV not implemented as it's for external use
                )
            }
        }

        item {
            TransferIntroSection(
                isExpanded = isIntroExpanded,
                onToggle = onToggleIntro
            )
        }
    }
}

@Composable
private fun TransferMethodCard(
    illustration: @Composable () -> Unit,
    title: String,
    speed: String,
    description: String,
    onSend: () -> Unit,
    onReceive: (() -> Unit)?
) {
    val secondary = MaterialTheme.colorScheme.secondary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            illustration()

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    title,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else Color.Black
                )
                Text(
                    speed,
                    color = secondary,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 0.dp)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onSend,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.btn_send))
                }
                onReceive?.let { action ->
                    OutlinedButton(
                        onClick = action,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium,
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.btn_receive))
                    }
                }
            }
        }
    }
}

@Composable
private fun RoundedIconBox(icon: ImageVector, modifier: Modifier = Modifier) {
    val grey = MaterialTheme.colorScheme.outline
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else Color.White,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        modifier = modifier.size(48.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun QrIllustration() {
    val grey = MaterialTheme.colorScheme.outline
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            RoundedIconBox(Icons.Default.QrCode2)
            Icon(Icons.Default.TrendingFlat, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
            RoundedIconBox(Icons.Default.CameraAlt)
        }
    }
}


@Composable
private fun QrSendScreen(chunks: List<String>, padding: PaddingValues) {
    if (chunks.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    val primary = MaterialTheme.colorScheme.primary

    LaunchedEffect(chunks) {
        while (true) {
            delay(600)
            currentIndex = (currentIndex + 1) % chunks.size
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
    ) {
        Text(
            stringResource(R.string.qr_receiver_instruction),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            val bitmap = remember(chunks[currentIndex]) {
                CryptoUtils.generateQRCode(chunks[currentIndex], 600)
            }
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = stringResource(R.string.desc_qr_code),
                modifier = Modifier.size(280.dp).padding(16.dp)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LinearProgressIndicator(
                progress = { (currentIndex + 1).toFloat() / chunks.size },
                modifier = Modifier.width(200.dp).height(8.dp).clip(CircleShape),
                color = primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer
            )
            Text(
                stringResource(R.string.qr_frame_progress, currentIndex + 1, chunks.size),
                style = MaterialTheme.typography.labelMedium,
                color = primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun QrReceiveScreen(
    viewModel: PasswordViewModel,
    importError: String?,
    pin: String,
    onSuccess: () -> Unit,
    padding: PaddingValues
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    var lastScanned by remember { mutableStateOf("") }

    val scannedCount by viewModel.scannedChunksCount.collectAsState()
    val total = viewModel.totalChunksExpected
    val isConsolidating = viewModel.isConsolidating

    Box(modifier = Modifier.fillMaxSize().padding(padding)) {
        // Camera preview as background
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val scanner = BarcodeScanning.getClient(BarcodeScannerOptions.Builder().setBarcodeFormats(FORMAT_QR_CODE, FORMAT_AZTEC).build())
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = androidx.camera.core.Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setTargetResolution(android.util.Size(1280, 720))
                        .build()
                    imageAnalyzer.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                            scanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    barcodes.firstOrNull()?.rawValue?.let { scanned ->
                                        if (scanned != lastScanned && !viewModel.isImportComplete) {
                                            lastScanned = scanned
                                            scope.launch {
                                                if (viewModel.processScannedChunk(scanned, pin)) onSuccess()
                                            }
                                        }
                                    }
                                }
                                .addOnCompleteListener { imageProxy.close() }
                        } else imageProxy.close()
                    }
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalyzer)
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay UI
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    if (total > 0) (if (isConsolidating) stringResource(R.string.qr_saving) else stringResource(R.string.qr_scanned_progress, scannedCount, total))
                    else stringResource(R.string.qr_point_camera),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            importError?.let {
                Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(8.dp)) {
                    Text(it, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(12.dp))
                }
            }

            if (total > 0) {
                LinearProgressIndicator(
                    progress = { scannedCount.toFloat() / total },
                    modifier = Modifier.fillMaxWidth().height(12.dp).clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
            }
        }
    }
}



@Composable
private fun InstructionStep(step: Int, text: String) {
    val primary = MaterialTheme.colorScheme.primary
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Top) {
        Surface(shape = CircleShape, color = primary, modifier = Modifier.size(28.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Text("$step", color = MaterialTheme.colorScheme.onPrimary, fontSize = 14.sp, fontWeight = FontWeight.Black)
            }
        }
        Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun PinInputDialog(title: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var pin by remember { mutableStateOf("") }
    val primary = MaterialTheme.colorScheme.primary

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Row(
                    modifier = Modifier.padding(start = 24.dp, end = 16.dp, top = 20.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = primary)
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = primary,
                        modifier = Modifier.weight(1f)
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))

                // Body
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PassCryptTextField(
                        value = pin,
                        onValueChange = { pin = it },
                        label = stringResource(R.string.label_pin),
                        isPassword = true
                    )
                }

                // Footer
                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel)) }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { if (pin.isNotBlank()) onConfirm(pin) },
                        enabled = pin.isNotBlank(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(stringResource(R.string.btn_confirm))
                    }
                }
            }
        }
    }
}

@Composable
private fun TransferIntroSection(
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.transfer_offline_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = primary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) stringResource(R.string.btn_collapse) else stringResource(R.string.btn_expand),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            androidx.compose.animation.AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TransferIntroItem(
                        icon = Icons.Default.WifiOff,
                        title = stringResource(R.string.transfer_intro_1_title),
                        shortDesc = stringResource(R.string.transfer_intro_1_short),
                        longDesc = stringResource(R.string.transfer_intro_1_long)
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    TransferIntroItem(
                        icon = Icons.Default.Security,
                        title = stringResource(R.string.transfer_intro_2_title),
                        shortDesc = stringResource(R.string.transfer_intro_2_short),
                        longDesc = stringResource(R.string.transfer_intro_2_long)
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    TransferIntroItem(
                        icon = Icons.Default.Code,
                        title = stringResource(R.string.transfer_intro_3_title),
                        shortDesc = stringResource(R.string.transfer_intro_3_short),
                        longDesc = stringResource(R.string.transfer_intro_3_long)
                    )
                }
            }
        }
    }
}

@Composable
private fun TransferIntroItem(
    icon: ImageVector,
    title: String,
    shortDesc: String,
    longDesc: String
) {
    var expanded by remember { mutableStateOf(false) }
    val primary = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier
                .size(24.dp)
                .padding(top = 2.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                if (expanded) longDesc else shortDesc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                if (expanded) stringResource(R.string.btn_show_less) else stringResource(R.string.btn_read_more),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = primary
            )
        }
    }
}


@Composable
fun FileIllustration() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            RoundedIconBox(Icons.Default.Lock)
            Icon(Icons.Default.TrendingFlat, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
            RoundedIconBox(Icons.Default.PictureAsPdf)
        }
    }
}

@Composable
private fun AppleIllustration() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            RoundedIconBox(Icons.Default.PhoneIphone)
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
            RoundedIconBox(Icons.Default.Password)
        }
    }
}

@Composable
private fun CsvExportScreen(
    onExport: () -> Unit,
    padding: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Text(
                    stringResource(R.string.csv_security_warning),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    stringResource(R.string.csv_import_how_to),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                InstructionStep(1, stringResource(R.string.csv_instr_1))
                InstructionStep(2, stringResource(R.string.csv_instr_2))
                InstructionStep(3, stringResource(R.string.csv_instr_3))
                InstructionStep(4, stringResource(R.string.csv_instr_4))
                InstructionStep(5, stringResource(R.string.csv_instr_5))
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onExport,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(Icons.Default.FileDownload, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text(stringResource(R.string.btn_export_csv_apple), fontWeight = FontWeight.Bold)
        }
    }
}
