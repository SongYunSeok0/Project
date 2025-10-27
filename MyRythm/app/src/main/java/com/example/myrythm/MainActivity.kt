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
import com.example.mypage.EditScreen
import com.example.mypage.HeartRateScreen
import com.example.mypage.mypageScreen
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
    var currentScreen by remember { mutableStateOf("MyPage") } // 기본 화면 설정

    Scaffold(
        topBar = { AppTopBar(
            when (currentScreen) { //탑바 이름 변경
                "Home" -> "내 정보 수정"
                "MyPage" -> "마이페이지"
                "Schedule" -> "심박수"
                else -> "마이 리듬"
            }.toString()
        ) },
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
                "Home" -> EditScreen()
                "MyPage" -> mypageScreen()
                "Schedule" -> HeartRateScreen()
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