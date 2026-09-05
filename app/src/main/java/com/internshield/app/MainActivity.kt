package com.internshield.app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.internshield.app.service.ShieldNotificationListener
import com.internshield.app.ui.components.GlassBottomBar
import com.internshield.app.ui.screens.DashboardScreen
import com.internshield.app.ui.screens.DemoSimulatorScreen
import com.internshield.app.ui.screens.DetectionDetailScreen
import com.internshield.app.ui.screens.RecentDetectionsScreen
import com.internshield.app.ui.screens.ReportsScreen
import com.internshield.app.ui.screens.ScanScreen
import com.internshield.app.ui.screens.SettingsScreen
import com.internshield.app.ui.screens.SplashScreen
import com.internshield.app.ui.theme.AccentOrange
import com.internshield.app.ui.theme.BgDark
import com.internshield.app.ui.theme.CardSurface
import com.internshield.app.ui.theme.InternShieldTheme
import com.internshield.app.ui.theme.TextMutedGray
import com.internshield.app.ui.theme.TextNearWhite
import com.internshield.app.ui.theme.TextWhite
import com.internshield.app.viewmodel.AppScreen
import com.internshield.app.viewmodel.ShieldViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ShieldViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register the BroadcastReceiver so live WhatsApp detections from
        // ShieldNotificationListener are forwarded to the ViewModel.
        viewModel.registerReceiver(this)

        // Enforce active system binding for NotificationListenerService
        ShieldNotificationListener.ensureConnected(this)

        setContent {
            InternShieldTheme {
                val contactPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    android.util.Log.d("MainActivity", "READ_CONTACTS permission: $isGranted")
                }

                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    android.util.Log.d("MainActivity", "POST_NOTIFICATIONS permission: $isGranted")
                }

                LaunchedEffect(Unit) {
                    if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        contactPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS)
                            != PackageManager.PERMISSION_GRANTED
                        ) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                NotificationPermissionGate()
                BatteryOptimizationGate()
                MainLayout(viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Guarantee the listener is rebound whenever app returns to foreground
        ShieldNotificationListener.ensureConnected(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.unregisterReceiver(this)
    }

    private fun isNotificationAccessGranted(): Boolean {
        val enabledListeners = Settings.Secure.getString(
            contentResolver,
            "enabled_notification_listeners"
        ) ?: return false
        return enabledListeners.contains(packageName)
    }

    private fun isIgnoringBatteryOptimizations(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            return pm.isIgnoringBatteryOptimizations(packageName)
        }
        return true
    }

    @Composable
    private fun NotificationPermissionGate() {
        val context = LocalContext.current
        var showDialog by remember { mutableStateOf(!isNotificationAccessGranted()) }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                containerColor = CardSurface,
                shape = RoundedCornerShape(20.dp),
                title = {
                    Text(
                        "Enable WhatsApp Notification Access",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = TextWhite
                    )
                },
                text = {
                    Text(
                        "InternShield needs Notification Access to analyze unknown incoming WhatsApp messages in real time.\n\n" +
                                "Saved contacts are kept private and never processed.",
                        color = TextNearWhite,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDialog = false
                            try {
                                context.startActivity(
                                    Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                )
                            } catch (e: Exception) {
                                context.startActivity(Intent(Settings.ACTION_SETTINGS))
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentOrange, contentColor = Color.White)
                    ) {
                        Text("Grant Access", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Later", color = TextMutedGray, fontSize = 13.sp)
                    }
                }
            )
        }
    }

    @Composable
    private fun BatteryOptimizationGate() {
        val context = LocalContext.current
        var showDialog by remember { mutableStateOf(isNotificationAccessGranted() && !isIgnoringBatteryOptimizations()) }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                containerColor = CardSurface,
                shape = RoundedCornerShape(20.dp),
                title = {
                    Text(
                        "Allow 24/7 Background Defense",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = TextWhite
                    )
                },
                text = {
                    Text(
                        "To ensure InternShield continues scanning WhatsApp messages after disconnecting from Android Studio or closing the app, please allow unrestricted background activity.",
                        color = TextNearWhite,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDialog = false
                            try {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                        data = Uri.parse("package:$packageName")
                                    }
                                    context.startActivity(intent)
                                }
                            } catch (e: Exception) {
                                try {
                                    context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                                } catch (e2: Exception) {
                                    // Ignore
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentOrange, contentColor = Color.White)
                    ) {
                        Text("Allow Background", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Later", color = TextMutedGray, fontSize = 13.sp)
                    }
                }
            )
        }
    }
}

@Composable
fun MainLayout(viewModel: ShieldViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    // Intercept Android back gesture / back button when inside sub-screens.
    // When on Home (Dashboard), BackHandler is disabled so pressing back directly closes the app.
    BackHandler(enabled = currentScreen !is AppScreen.Dashboard && currentScreen !is AppScreen.Splash) {
        viewModel.handleBack()
    }

    val showBottomBar = currentScreen is AppScreen.Dashboard ||
            currentScreen is AppScreen.Scan ||
            currentScreen is AppScreen.RecentDetections

    Scaffold(
        containerColor = BgDark,
        bottomBar = {
            if (showBottomBar) {
                GlassBottomBar(
                    currentScreen = currentScreen,
                    onNavigate = { screen -> viewModel.navigateTo(screen) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BgDark)
                .padding(innerPadding)
        ) {
            when (val screen = currentScreen) {
                is AppScreen.Splash -> SplashScreen(viewModel)
                is AppScreen.Dashboard -> DashboardScreen(viewModel)
                is AppScreen.RecentDetections -> RecentDetectionsScreen(viewModel)
                is AppScreen.Scan -> ScanScreen(viewModel)
                is AppScreen.Reports -> ReportsScreen(viewModel)
                is AppScreen.DetectionDetail -> DetectionDetailScreen(screen.result, viewModel)
                is AppScreen.Settings -> SettingsScreen(viewModel)
                is AppScreen.DemoSimulator -> DemoSimulatorScreen(viewModel)
            }
        }
    }
}
