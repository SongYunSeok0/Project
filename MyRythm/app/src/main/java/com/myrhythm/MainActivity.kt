package com.myrhythm

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.messaging.FirebaseMessaging
import com.data.core.push.FcmTokenStore
import com.data.core.push.PushManager
import com.myrhythm.ui.theme.MyRhythmTheme
import com.myrhythm.viewmodel.InitFcmTokenViewModel
import com.myrythm.AppRoot
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val fcmViewModel: InitFcmTokenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fcmViewModel.initFcmToken()
        askNotificationPermission()

        setContent { AppRoot() }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val perm = android.Manifest.permission.POST_NOTIFICATIONS
            if (checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(perm), 1001)
            }
        }
    }
}




@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    MyRhythmTheme { AppRoot() }
}


