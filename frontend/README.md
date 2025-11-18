# Oddventure Frontend

CS2 E-Sports Betting Platform의 프론트엔드 애플리케이션입니다.

## 기술 스택

- **React 18** - UI 라이브러리
- **TypeScript** - 타입 안정성
- **Vite** - 빌드 도구
- **React Router DOM** - 라우팅
- **Axios** - HTTP 클라이언트

## 시작하기

### 사전 요구사항

- Node.js 18 이상
- npm 또는 yarn

### 설치

```bash
# 의존성 설치
npm install

# 환경 변수 설정
cp .env.example .env
# .env 파일에서 API_BASE_URL 설정

# 개발 서버 실행
npm run dev
```

### 사용 가능한 스크립트

```bash
# 개발 서버 실행 (http://localhost:5173)
npm run dev

# 프로덕션 빌드
npm run build

# 빌드 결과 미리보기
npm run preview

# 린트 검사
npm run lint
```

## 프로젝트 구조

```
frontend/
├── src/
│   ├── components/        # 재사용 가능한 컴포넌트
│   ├── contexts/          # React Context (AuthContext)
│   ├── layouts/           # 레이아웃 컴포넌트 (Header, Footer, MainLayout)
│   ├── pages/             # 페이지 컴포넌트
│   │   ├── user/          # 유저 페이지 (로그인, 회원가입, 경기, 배팅)
│   │   ├── admin/         # 관리자 페이지
│   │   └── HomePage.tsx   # 홈페이지
│   ├── services/          # API 서비스 (axios)
│   ├── types/             # TypeScript 타입 정의
│   ├── App.tsx            # 메인 앱 컴포넌트 (라우팅)
│   ├── main.tsx           # 엔트리 포인트
│   └── index.css          # 글로벌 스타일
├── .env                   # 환경 변수
├── .env.example           # 환경 변수 예시
├── package.json           # 의존성 관리
└── vite.config.ts         # Vite 설정
```

## 주요 기능

### 사용자 기능

- **회원가입/로그인**: JWT 기반 인증
- **경기 목록**: 실시간 경기 정보 조회
- **배팅**: 경기에 대한 배팅 생성
- **내 배팅**: 배팅 내역 조회 및 취소

### 관리자 기능

- **경기 관리**: GRID API에서 경기 가져오기, 결과 업데이트
- **사용자 관리**: 사용자 목록 조회, 포인트 조정
- **Elasticsearch 동기화**: 수동 동기화 트리거

## 인증 시스템

- **Access Token**: LocalStorage에 저장, 1시간 유효
- **Refresh Token**: HttpOnly 쿠키에 저장, 7일 유효
- 자동 토큰 갱신 (Axios Interceptor)

## 스타일 가이드

### 색상 팔레트 (Gray Vibe)

- **Primary**: \`#2d2d2d\` (Dark Gray)
- **Secondary**: \`#6b6b6b\` (Medium Gray)
- **Background**: \`#1a1a1a\`
- **Background Light**: \`#242424\`
- **Text**: \`#e0e0e0\`
- **Text Muted**: \`#9e9e9e\`

### CSS 변수 사용

```css
.my-component {
  background-color: var(--color-bg);
  color: var(--color-text);
  padding: var(--spacing-md);
  border-radius: var(--radius-sm);
}
```

## 라이선스

MIT
