package com.example.testmmp

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(0xFFEDEDEE.toInt(), 0xFFEDEDEE.toInt()),
        )
        setContent {
            App(registerBackHandler = { enabled, onBack -> BackHandler(enabled, onBack) }, onShareLink = { link ->
                startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, link)
                }, "Поделиться ссылкой"))
            })
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
