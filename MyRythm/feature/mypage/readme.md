# :feature:mypage

사용자 정보/프로필 편집, 디바이스 등록(QR/Barcode), 리포트 진입 등 “마이페이지” 영역을 담당합니다.

## Scope
- 프로필/내 정보 UI
- QR/바코드 스캔(ML Kit Barcode)
- CameraX 기반 스캔 UI
- (연동) 디바이스 등록 플로우 진입점

## Key Tech
- ML Kit Barcode Scanning
- CameraX
- Hilt + Compose Navigation

## Flow (예: 디바이스 등록 개요)
UI(Scan/입력)
 → ViewModel
 → UseCase
 → Repository
 → data(서버 등록/로컬 저장)

## 의도
- 디바이스 등록처럼 복잡한 플로우를 feature 내부에서 단계화
- 공통 에러 표현(UiError 등)을 shared로 통일해 UX 일관성 확보
