package com.myrhythm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.myrhythm.ui.theme.MyRhythmTheme
import com.myrythm.AppRoot
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppRoot() }
    }
}



@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    MyRhythmTheme { AppRoot() }
}
