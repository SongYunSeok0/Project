package com.shared.ui.theme

import androidx.compose.ui.graphics.Color

// 기본 컬러
// 기본 컬러가 Color.White 처럼 컴포즈상수에 존재하는 경우, color.kt에 정의하지 말고 theme.kt에 바로 넣기.
val Black40 = Color(0x66000000)     // 블랙+투명도40, 가이드글씨
val AuthBlue = Color(0xFF6AC0E0)
val BorderGray = Color(0xFF808080)

val DividerColor = Color(0xFFE5E5E5)


// 메인 화면 컬러 m3테마용
val Primary = Color(0xFF6AE0D9)
val Secondary = Color(0x4DB5E5E1)
val OnSurfaceVariant = Color(0xFF5DB0A8)

val SurfaceVariant = Color(0xFFB4B4B4)

// 컴포넌트
val InquiryCardAnswer = Color(0xFFDFFDFB)
val InquiryCardQuestion = Color(0xFFE4F5F4)
val ChatbotCard = Color(0x3380E1FF)
val SchedulerCard = Color(0x33EB80FF)
val StepCard = Color(0x33AD9ABC)
val RateCard = Color(0x33FF7367)
val TimeRemainingCard = Color(0x3320FFE5)
val MapCard = Color(0x33C5FF80)
val NewsCard = Color(0x33FFEF6C)
val HealthInsightCard = Color(0x666AC0E0)
val MainFeatureCardBorderStroke = Color(0xfff3f4f6)
val AppTransparent = Color.Transparent
val HeartRateCardGradientLight = Color(0xFFFFE8E8)
val HeartRateCardGradientDark = Color(0xFFFFD5D5)
val HeartRateLowColor = Color(0xFF3B82F6)
val HeartRateNormalColor = Color(0xFF16A34A)
val HeartRateWarningColor = Color(0xFFEF4444)
val CompletionCautionColor = Color(0xFFF59E0B)
val BookMarkColor = Color(0xFFFFC107)




// 로그인프로세스
val AuthBackground = Color(0xFFB5E5E1)  //메인배경
val AuthPrimaryButton = Color(0xFFFFFFFF)  //메인버튼바탕
val AuthOnPrimary = Color(0x66000000)   // 메인버튼위에올라갈글씨 블랙+투명도40
val AuthSecondrayButton = Color(0xFF6AC0E0)     //서브버튼 바탕
val AuthOnSecondray = Color(0xFFFFFFFF)     // 하얀색글씨
val AuthAppName = OnSurfaceVariant    //앱 제목 컬러

val AuthSurface = Color.White                // 입력필드
val AuthOnFieldHint = Black40               // 입력필드 위 가이드 글씨
val AuthOnSurface = Color.Black              // 입력필드 위 사용자 글씨
val AuthPrimaryButtonClick = Color(0x806AC0E0)       // 메인 버튼 클릭 시 투명도 50 컬러 변동


// 스플래시+로그인 +)커스텀토큰으로 별도 활용
val LoginBackground = Primary               // 로그인화면의 메인배경
val LoginSurface = Color.White              // 입력 필드
val LoginOnFieldHint = Black40              // 입력필드 위 가이드 글씨
val LoginOnSurface = Color.Black             // 입력필드 위 사용자 글씨
val LoginPrimaryButton = AuthBlue                  // 로그인화면의 메인버튼
val LoginOnPrimary = Color.White                    // 로그인화면의 메인버튼 위 글씨
val LoginSecondrayButton = Color.White              // 로그인화면의 서브버튼
val LoginOnSecondray = AuthBlue                    // 로그인화면의 서브버튼 위 글씨
val LoginAppName = Color(0xFFC9F8F6)    // 앱 제목 컬러_이미지 말고 글씨 넣을 시 사용
val LoginTertiary = Color(0xFF77A3A1)              // 그 외_안내메시지 폰트 컬러


/*
100% (불투명)	FF
80%	CC
60%	99
40%	66
20%	33
0% (완전 투명)	00
 */