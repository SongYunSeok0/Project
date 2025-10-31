package com.myrythm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.myrythm.ui.theme.MyRythmTheme




class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { MyRythmTheme { AppRoot() } }
    }
}



@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    MyRythmTheme { AppRoot() }
}
