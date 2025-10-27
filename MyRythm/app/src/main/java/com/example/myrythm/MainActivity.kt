package com.example.myrythm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode.Companion.Screen
import androidx.compose.ui.tooling.preview.Preview
import com.example.design.AppBottomBar
import com.example.myrythm.ui.theme.MyRythmTheme
import com.example.design.AppTopBar
import com.example.login.LoginScreen
import com.example.login.PwdScreen
import com.example.login.SignupScreen
import com.example.main.MainScreen
import com.example.map.MapScreen
import com.example.mypage.EditScreen
import com.example.mypage.HeartRateScreen
import com.example.mypage.MyPageScreen
import com.example.news.NewsMainScreen
import com.example.news.NewsScreen
import com.example.scheduler.CameraScreen
import com.example.scheduler.PrescriptionScanScreen
import com.example.scheduler.RegiScreen
import com.example.scheduler.SchedulerScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyRythmTheme {
                AppRoot()
            }
        }
    }
}

@Composable
fun AppRoot() {
    var currentScreen by remember { mutableStateOf("SignUp") } // 기본 화면 설정

    Scaffold(
        topBar = { if( currentScreen !in listOf("Home", "News", "Map","Login","Pwd","SignUp")){
            AppTopBar(
                when (currentScreen) { //탑바 이름 변경
                    "Home" -> " "
                    "MyPage" -> "마이페이지"
                    "Schedule" -> "일정"
                    "Regi" -> "스케줄 등록"
                    "Camera" -> "처방전 인식"
                    "Regi" -> "처방전 등록"
                    "Heart" -> "심박수"
                    "Edit" -> "내 정보 수정"
                    else -> "마이 리듬"
                }.toString()
            )
        } },
        bottomBar = {
            AppBottomBar(
                currentScreen = currentScreen,
                onTabSelected = { screen ->
                    currentScreen = screen
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            when (currentScreen) {
                "Home" -> MainScreen()
                "MyPage" -> MyPageScreen()
                "Schedule" -> SchedulerScreen()
                "Camera" -> CameraScreen()
                "OCR" -> PrescriptionScanScreen()
                "Regi" -> RegiScreen()
                "Edit" -> EditScreen()
                "Heart" -> HeartRateScreen()
                //"Chat" -> ChatBotScreen()
                "Map" -> MapScreen()
                "Login"-> LoginScreen()
                "Pwd" -> PwdScreen()
                "SignUp"-> SignupScreen()
                //"News" -> NewsScreen()
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyRythmTheme {
        AppRoot()
    }
}