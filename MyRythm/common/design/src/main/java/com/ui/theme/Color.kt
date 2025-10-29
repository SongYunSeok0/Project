package com.ui.theme

import androidx.compose.ui.graphics.Color

//앱의 모든 색상 팔레트 정의. (PrimaryColor, LightColorScheme, DarkColorScheme 등).
val PrimaryLight = Color(0xFF6AE0D9)
val SecondaryLight = Color(0xFF6AC0E0)
val PrimaryText = Color(0xFF000000)
val PointText = Color(0xFF6AC0E0)

// 스플래시+로그인
val AuthLoginBackground = Color(0xFF6AE0D9)
val AuthLoginButton = Color(0xFF6AC0E0)
val AuthLoginAppName = Color(0xFFC9F8F6)    // 앱 제목 컬러
val AuthLoginText = Color(0xFF77A3A1)    //비밀번호를 잊?메시지
val AuthLoginSecondrayButton = Color(0xFFFFFFFF)     //서브버튼 바탕

val AuthLoginOnSecondray = Color(0x66000000)     // 블랙+투명도40


// 로그인프로세스
val AuthBackground = Color(0xFFB5E5E1)  //메인배경
val AuthPrimaryButton = Color(0xFFFFFFFF)  //메인버튼바탕
val AuthOnPrimary = Color(0x66000000)   // 메인버튼위에올라갈글씨 블랙+투명도40
val AuthSecondrayButton = Color(0xFF6AC0E0)     //서브버튼 바탕
val AuthOnSecondray = Color(0xFFFFFFFF)     // 하얀색글씨
val AuthAppName = Color(0xFF5DB0A8)    //앱 제목 컬러

// 로그인프로세스 화면
/*
val AuthColorScheme = authColorScheme(
    authbackground = AuthBackground,
    authbutton = AuthPrimaryButton,
    authOnPrimary = AuthOnPrimary,
    authText = AuthLoginText,
    authSecondaryButton = AuthSecondrayButton,
    authOnSecondary = AuthOnSecondray,
    authAppName = AuthAppName,
)

 */


/*
💡 주요 역할 설명
색상 역할 (Role)	설명	컴포넌트 예시
primary	앱의 주된 브랜드 색상.	Extended Floating Action Button (FAB), Switch On 상태, Slider 활성 상태, 기본 **TopAppBar**의 배경
onPrimary	primary 색상 위에 배치되는 콘텐츠(텍스트/아이콘) 색상.	primary 버튼의 텍스트 색상
primaryContainer	primary보다 명도가 낮은 강조된 영역의 배경색.	강조된 Chip의 배경색
surface	컴포넌트 배경의 기본 색상 (예: 카드, 다이얼로그, 바텀 시트).	Card 배경, ModalBottomSheet 배경
onSurface	surface 위에 배치되는 콘텐츠(텍스트/아이콘) 색상.	일반적인 본문 텍스트 색상
background	스크롤 가능한 콘텐츠가 없는 가장 큰 영역의 배경색.	Scaffold의 기본 배경색

100% (불투명)	FF
80%	CC
60%	99
40%	66
20%	33
0% (완전 투명)	00


*LightColorScheme**에 할당해야 합니다.

Kotlin

// design/ui/theme/Color.kt의 일부

val PrimaryLightColor = Color(0xFF6AE0D9) // val 이름 변경

val LightColorScheme = lightColorScheme(
    primary = PrimaryLightColor, // ⬅️ 여기에 할당
    onPrimary = Color.Black,
    secondary = SecondaryLight,
    // ...
    background = Color(0xFFFFFFFF) // ⬅️ MainBackground 역할도 여기에 할당하는 것이 좋음
)
B. MainScreen.kt에서 역할로 호출
MainScreen.kt에서는 **MaterialTheme.colorScheme**를 통해 primary 역할을 호출합니다. 이 경우 별도의 import 없이 테마가 적용됩니다.

Kotlin

package com.sesac.app

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme // ⬅️ MaterialTheme을 가져옵니다.
// ... (MainBackground import는 제거)

@Composable
fun MainScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            // 1. 테마의 background 역할 색상을 사용합니다.
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        // ...
    }

    // 2. 버튼에 테마의 primary 역할 색상을 사용합니다.
    CustomButton(
        onClick = { /* ... */ },
        modifier = Modifier.background(MaterialTheme.colorScheme.primary)
    ) {
        // ...
    }
}
결론:

**MainBackground**처럼 M3 역할에 정확히 매핑되지 않는 커스텀 색상은 val을 직접 import하여 사용하는 것이 편리합니다.

**PrimaryLight**처럼 Material Design의 핵심 색상 역할을 하는 값은 ColorScheme에 할당한 뒤, **MaterialTheme.colorScheme.primary**로 호출하는 것이 다크 모드 등 테마 관리에 유리합니다.

 */