package com.axiondesktop

import android.app.Presentation
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.view.Display
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.pm.ResolveInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Navy = Color(0xFF101827)
private val Panel = Color(0xCC172338)
private val Cyan = Color(0xFF62D6E8)
private val TextPrimary = Color(0xFFF4F7FB)

class MainActivity : ComponentActivity() {
    private lateinit var displayManager: DisplayManager
    private var desktopPresentation: DesktopPresentation? = null

    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = showExternalDesktopIfAvailable()
        override fun onDisplayRemoved(displayId: Int) {
            desktopPresentation?.dismiss()
            desktopPresentation = null
        }
        override fun onDisplayChanged(displayId: Int) = Unit
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        displayManager.registerDisplayListener(displayListener, null)

        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                primary = Cyan,
                background = Navy,
                surface = Panel,
                onBackground = TextPrimary,
                onSurface = TextPrimary
            )) {
                DesktopHome(
                    onOpenApp = ::launchApp,
                    onRefreshDisplays = ::showExternalDesktopIfAvailable
                )
            }
        }
        showExternalDesktopIfAvailable()
    }

    private fun launchApp(info: ResolveInfo) {
        val packageName = info.activityInfo?.packageName ?: return
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName) ?: return
        try { startActivity(launchIntent) } catch (_: Exception) { }
    }

    private fun showExternalDesktopIfAvailable() {
        val displays = displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION)
        val display = displays.firstOrNull() ?: return
        runOnUiThread {
            try {
                desktopPresentation?.dismiss()
                desktopPresentation = DesktopPresentation(this, display).also { it.show() }
            } catch (_: Exception) {
                desktopPresentation = null
            }
        }
    }

    override fun onDestroy() {
        displayManager.unregisterDisplayListener(displayListener)
        desktopPresentation?.dismiss()
        desktopPresentation = null
        super.onDestroy()
    }
}

@Composable
private fun DesktopHome(
    onOpenApp: (ResolveInfo) -> Unit,
    onRefreshDisplays: () -> Unit
) {
    val context = LocalContext.current
    var appDrawerOpen by remember { mutableStateOf(false) }
    var settingsOpen by remember { mutableStateOf(false) }
    var apps by remember { mutableStateOf(loadLaunchableApps(context)) }
    var time by remember { mutableStateOf(currentTime()) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(15_000)
            time = currentTime()
            apps = loadLaunchableApps(context)
        }
    }

    Box(
        Modifier.fillMaxSize().background(
            Brush.linearGradient(listOf(Color(0xFF13213B), Color(0xFF23516A), Color(0xFF172338)))
        )
    ) {
        Column(Modifier.fillMaxSize().padding(28.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("AXION DESKTOP", fontSize = 13.sp, color = Cyan, fontWeight = FontWeight.Bold)
                    Text("Your Android. A bigger workspace.", fontSize = 26.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                }
                Surface(color = Panel, shape = RoundedCornerShape(18.dp)) {
                    Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Wifi, contentDescription = null, tint = Cyan)
                        Spacer(Modifier.width(8.dp))
                        Text(time, color = TextPrimary)
                    }
                }
            }

            Spacer(Modifier.height(28.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                DesktopTile("Apps", "Open installed applications", Icons.Default.Apps, Modifier.weight(1f)) {
                    apps = loadLaunchableApps(context)
                    appDrawerOpen = true
                }
                DesktopTile("Displays", "Find a connected monitor", Icons.Default.DesktopWindows, Modifier.weight(1f)) {
                    onRefreshDisplays()
                }
                DesktopTile("Settings", "Desktop preferences", Icons.Default.Settings, Modifier.weight(1f)) {
                    settingsOpen = true
                }
            }

            Spacer(Modifier.weight(1f))
            Text("Tip: connect a Bluetooth mouse and keyboard for a desktop-like experience.",
                color = TextPrimary.copy(alpha = 0.8f), fontSize = 14.sp)
        }

        Row(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp)
                .background(Panel, RoundedCornerShape(22.dp)).padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalButton(onClick = {
                apps = loadLaunchableApps(context)
                appDrawerOpen = true
            }) { Icon(Icons.Default.Apps, contentDescription = "Apps"); Spacer(Modifier.width(6.dp)); Text("Start") }
            Spacer(Modifier.width(12.dp))
            Text("Axion Desktop", color = TextPrimary, modifier = Modifier.weight(1f))
            Text(time, color = Cyan, modifier = Modifier.padding(end = 8.dp))
        }

        if (appDrawerOpen) {
            AlertDialog(
                onDismissRequest = { appDrawerOpen = false },
                title = { Text("Applications") },
                text = {
                    if (apps.isEmpty()) Text("No launchable apps were found.")
                    else LazyVerticalGrid(columns = GridCells.Adaptive(110.dp), verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 440.dp)) {
                        items(apps) { app ->
                            val label = app.loadLabel(context.packageManager).toString()
                            Surface(color = Panel, shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.clickable {
                                    onOpenApp(app)
                                    appDrawerOpen = false
                                }) {
                                Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(label.take(2).uppercase(), color = Cyan, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(6.dp))
                                    Text(label, color = TextPrimary, fontSize = 12.sp, maxLines = 2)
                                }
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { appDrawerOpen = false }) { Text("Close") }
                }
            )
        }

        if (settingsOpen) {
            AlertDialog(
                onDismissRequest = { settingsOpen = false },
                title = { Text("Desktop settings") },
                text = { Text("Prototype build: external display detection is automatic when Android exposes a presentation display. Display output and resizable app windows depend on device hardware and Android ROM support.") },
                confirmButton = { TextButton(onClick = { settingsOpen = false }) { Text("OK") } }
            )
        }
    }
}

@Composable
private fun DesktopTile(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(modifier = modifier.clickable(onClick = onClick), color = Panel, shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(18.dp)) {
            Icon(icon, contentDescription = null, tint = Cyan, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(18.dp))
            Text(title, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = TextPrimary.copy(alpha = 0.72f), fontSize = 12.sp)
        }
    }
}

private fun loadLaunchableApps(context: Context): List<ResolveInfo> {
    val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
    return context.packageManager.queryIntentActivities(intent, 0)
        .sortedBy { it.loadLabel(context.packageManager).toString().lowercase(Locale.getDefault()) }
}

private fun currentTime(): String =
    SimpleDateFormat("EEE, dd MMM  HH:mm", Locale.getDefault()).format(Date())

private class DesktopPresentation(context: Context, display: Display) : Presentation(context, display) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        setContentView(android.widget.FrameLayout(context).apply {
            setBackgroundColor(android.graphics.Color.rgb(19, 33, 59))
            val title = android.widget.TextView(context).apply {
                text = "AXION DESKTOP\n\nExternal display connected\n\nConnect a mouse and keyboard to get started."
                textSize = 26f
                setTextColor(android.graphics.Color.WHITE)
                gravity = android.view.Gravity.CENTER
                setPadding(40, 40, 40, 40)
            }
            addView(title, android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT
            ))
        })
    }
}
