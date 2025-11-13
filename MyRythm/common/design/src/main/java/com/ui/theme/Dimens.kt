package com.ui.theme

import androidx.compose.ui.unit.dp

// dp나 sp와 같은 공용 크기 값을 정의. (예: val PaddingMedium = 16.dp).



/*
필요한 거
<로그인 - 회원가입>
입력 필드 사이즈
메인 버튼
안내메시지
서브 버튼
로고 알약 이미지 사이즈 (인증 전체 프로세스에)
+ 로고 글씨 이미지 사이즈(로그인,비밀번호잊음 창에만 이미지 존재)

각각의 간격도 체크

<메인>
탑바의 뒤로가기 아이콘+글씨 중앙+탑바 영역 사이즈
바텀바의 아이콘 사이즈+바텀바 영역 사이즈+바텀바 버튼 사이즈
등등등
 */

// 기본 간격  251030 11:31아직미편집본
val PaddingSmall = 8.dp
val PaddingMedium = 16.dp
val PaddingLarge = 24.dp
val PaddingExtraLarge = 32.dp
val IconSizeStandard = 24.dp


// <스플래시+로그인+회원가입 등> Auth

// 인증화면의 기본 입력 필드 사이즈
val AuthFieldWidth = 318.dp
val AuthFieldHeight = 56.dp

// 인증 화면의 기본 입력 필드 글씨 사이즈는 16.dp 정도
val AuthSpacingVertical = 15.dp                     // 입력 필드 및 버튼 간의 기본 간격
val AuthButtonHeight  = AuthFieldHeight             // 인증 프로세스의 버튼 높이 = 입력필드 높이


// 로고+글씨 둘 다 쓰는 경우의 로고 이미지 크기
val AuthLogoSize = 180.dp
val AuthTextLogoWidth = 320.dp
val AuthTextLogoHeight = 96.dp


// 기본 그림자 높이 (입력 필드, 메인 버튼)
val ShadowElevationDefault = 1.dp
// 로그인 링크 버튼의 높은 그림자 높이
val ShadowElevationLink = 4.dp



/*
package com.sesac.design.ui.theme

import androidx.compose.ui.unit.dp

// =====================================================================
// [A] Spacing & Padding (간격)
// =====================================================================
val PaddingSmall = 8.dp
val PaddingMedium = 16.dp
val PaddingLarge = 24.dp
val PaddingExtraLarge = 32.dp

// =====================================================================
// [B] Component Fixed Sizes (컴포넌트 고정 크기)
// =====================================================================

// 입력 필드 고정 크기 (Auth 또는 Global로 사용)
val AuthFieldWidth = 318.dp
val AuthFieldHeight = 56.dp

// 버튼 고정 높이
val PrimaryButtonHeight = 48.dp

// =====================================================================
// [C] Icon & Image Sizes (아이콘 및 이미지 크기)
// =====================================================================

// 스플래시 화면 로고 크기 (재사용성이 낮아도 정의하여 디자인 토큰 관리)
val SplashLogoSize = 240.dp // 예시 값: 일반 로고보다 크게 설정

// 로고 이미지 크기
val AppLogoSizeMedium = 64.dp
val AppLogoSizeLarge = 96.dp

// Auth Screen Logo Sizes (인증 화면 전용 로고 및 타이틀 크기 추가)
// 기존 고정 크기 대신, 반응형을 위한 최소/최대 크기를 정의합니다.
val AuthLogoIconSizeMax = 180.dp // 최대 크기 (기존의 180dp는 최대값으로 사용)
val AuthLogoIconSizeMin = 100.dp // 최소 크기
val AuthLogoTitleWidthMax = 320.dp // 최대 폭 (기존의 320dp는 최대값으로 사용)
val AuthLogoTitleWidthMin = 180.dp // 최소 폭
val AuthLogoTitleHeight = 96.dp // 높이는 여전히 고정될 수 있지만, 비율을 위해 사용될 수 있습니다.
val AuthLogoInternalSpacing = 10.dp // 아이콘과 타이틀 이미지 사이 간격

// 일반 아이콘 크기 (버튼, 탭바 등)
val IconSizeDefault = 24.dp
val IconSizeLarge = 32.dp


 */
