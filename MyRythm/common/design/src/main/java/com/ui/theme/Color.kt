package com.ui.theme

import androidx.compose.ui.graphics.Color

// 기본 컬러
val BasicWhite = Color(0xFFFFFFFF)
val BasicBlack = Color(0xFF000000)
val Black40 = Color(0x66000000)     // 블랙+투명도40, 가이드글씨
val AuthBlue = Color(0xFF6AC0E0)

// 메인 화면 컬러 m3테마용
val BackGround = BasicWhite
val Primary = Color(0xFF6AE0D9)
val OnPrimary = BasicWhite
val Surface = Color(0xFFF9FAFB)
val OnSurface = BasicBlack
/*
val Primary = Color(0xFF6AE0D9)
val BackGround = BasicWhite
val Surface =  // 입력 필드가 아닌, 카드나 시트 등의 표면색
val Secondary = // 보조 강조색
val OnPrimary = // Primary 위에 올라가는 글씨
val OnSecondary =// Secondary 위에 올라가는 글씨
val OnSurface = // Surface 위에 올라가는 일반적인 본문 텍스트
 */


// 회원가입프로세스 +)커스텀토큰으로 별도 활용
val AuthBackground = Color(0xFFB5E5E1)      // 배경
val AuthSurface = BasicWhite                // 입력필드
val AuthOnFieldHint = Black40               // 입력필드 위 가이드 글씨
val AuthOnSurface = BasicBlack              // 입력필드 위 사용자 글씨
val AuthPrimaryButton = AuthBlue                     // 메인 버튼
val AuthPrimaryButtonClick = Color(0x806AC0E0)       // 메인 버튼 클릭 시 투명도 50 컬러 변동
val AuthOnPrimary = BasicWhite                       // 메인 버튼 위 글씨
val AuthSecondrayButton = BasicWhite                 // 서브 버튼
val AuthOnSecondray = AuthBlue                       // 서브 버튼 위 글씨
val AuthAppName = Color(0xFF5DB0A8)                  // 앱 제목 컬러_이미지 말고 글씨 넣을 시 사용

// 스플래시+로그인 +)커스텀토큰으로 별도 활용
val LoginBackground = Primary               // 로그인화면의 메인배경
val LoginSurface = BasicWhite               // 입력 필드
val LoginOnFieldHint = Black40              // 입력필드 위 가이드 글씨
val LoginOnSurface = BasicBlack             // 입력필드 위 사용자 글씨
val LoginPrimaryButton = AuthBlue                  // 로그인화면의 메인버튼
val LoginOnPrimary = BasicWhite                    // 로그인화면의 메인버튼 위 글씨
val LoginSecondrayButton = BasicWhite              // 로그인화면의 서브버튼
val LoginOnSecondray = AuthBlue                    // 로그인화면의 서브버튼 위 글씨
val LoginAppName = Color(0xFFC9F8F6)    // 앱 제목 컬러_이미지 말고 글씨 넣을 시 사용
val LoginTertiary = Color(0xFF77A3A1)              // 그 외_안내메시지 폰트 컬러


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

 */