# NBE4-5-3-Team09

프로그래머스 백엔드 데브코스 4기 5회차 9팀 시고르백구의 2차 팀 프로젝트입니다.

## 👥 Team Introduction

| Name   | GitHub                                    | Role                                                                                         |
| ------ | ----------------------------------------- | -------------------------------------------------------------------------------------------- |
| 이화영 | [2hwayoung](https://github.com/2hwayoung) | 팀 리딩, 프로젝트 관리, Upbit WebSocket 연동, 호가 정보 조회, 환경변수 관리, Gihub Action CI |
| 김하연 | [xaxeon](https://github.com/xaxeon)       | 글로벌 예외 처리, 마켓 목록 조회 및 코인 대시보드, 북마크 추가/삭제, 소셜 로그인             |
| 이승민 | [min429](https://github.com/min429)       | 웹소켓 클라이언트(프론트), 현재가 조회, 푸시 알림, Swagger 커스텀, Gihub Action CD           |
| 장무영 | [wkdan](https://github.com/wkdan)         | 사용자 인증, 캔들 차트 조회, 채팅 기능, 관련 뉴스 기능, 관리자 권한 및 Security 설정         |

## 📊 Project Overview

### 프로젝트 이름: **코잉(Coing)**

_💡 실시간 코인 데이터 분석 및 북마크 대시보드 서비스_

### 운영 사이트: https://coing-ashen.vercel.app

이 프로젝트의 목표는 실시간 코인 데이터를 기반으로, 개인화된 투자 인사이트를 제공하는 웹 서비스 **코잉(Coing)**을 구축하는 것입니다.
업비트 OpenAPI 및 WebSocket을 활용해 다양한 암호화폐 관련 데이터를 실시간으로 수집하고,
사용자가 관심 있는 코인을 북마크하여 개인화된 대시보드를 구성할 수 있도록 하여 투자자들의 편의성과 효율적인 의사결정을 돕는 것이 목적입니다.

단순한 시세 제공이 아닌, 호가, 체결, 캔들, 기술적 지표 등의 다양한 데이터를 수집하여 투자 인사이트를 제공합니다.
기존의 CryptoQuant, CoinMarketCap, Coin360 등 다양한 서비스를 벤치마킹했으며, 시각화 + 개인화 + 실시간성에 중점을 두었습니다.

<img width="1566" alt="image" src="https://github.com/user-attachments/assets/69fbc43f-aaa6-4eaf-b051-b89396ecd3bc" />

---

### 🛠 Kotlin Migration (2차 MVP 핵심)

이번 2차 MVP에서는 기존 Java 기반 백엔드 코드를 전면적으로 Kotlin으로 마이그레이션하여 유지보수성과 생산성을 향상시켰습니다.

- Kotlin DSL 기반 Gradle 빌드 환경 구성

- data class, sealed class, extension, coroutines 등 Kotlin 특화 문법 도입

- Mockito 대신 mockito-kotlin을 활용한 Kotlin 친화 테스트 환경 구축

- WebSocket/REST API 흐름, 서비스/도메인 계층을 Kotlin스럽게 리팩토링

> ✅ 타입 안정성과 간결함을 바탕으로, 빠른 개발과 명확한 도메인 표현이 가능해졌습니다.

## 최소 요구사항 (MVP)

프로젝트의 기본 기능은 다음과 같습니다:

1. **사용자 인증**
   - 이메일 인증 기반 회원가입, 로그인/로그아웃
2. **코인 대시보드**
   - 전체 코인 목록 조회
   - 종목/마켓 단위 시세, 체결, 호가, 다양한 캔들 차트(초/분/일/주/월/년 단위)
   - 기술적 지표 추가(Spread, Imbalance, Liquidity Depth 등)
3. **북마크 기능**
   - 관심 있는 코인 북마크 등록/삭제
   - 북마크한 종목/마켓 단위 현재가, 체결가 및 호가 정보 조회
4. **실시간 데이터 처리**
   - WebSocket 기반 시세/체결/호가 실시간 업데이트
   - REST API 기반 캔들 데이터 제공(최소 1초 간격 Polling)

## 2차 MVP (추가 기능)

MVP 외에 추가적으로 구현된 기능들은 다음과 같습니다:

- **인증/관리 기능**
  - 소셜 로그인 (카카오 연동)
  - 관리자 기능: 채팅 신고 관리
- **투자 인사이트 기능**
  - 특정 조건(예: 가격 급등락, 거래량 급증 등) 설정 시 푸시 알림 전송
  - Naver 검색 API 기반 실시간 뉴스 연동 (코인 키워드 기반)
- **커뮤니티 기능**
  - WebSocket 기반 종목 단위 실시간 채팅 기능

#### 관련 문서

> [📝 코잉(Coing) 기획서](<https://github.com/prgrms-be-devcourse/NBE4-5-3-Team09/wiki/%EC%BD%94%EC%9E%89(Coing)-%EA%B8%B0%ED%9A%8D%EC%84%9C>)

> (이전) [📘 코잉(Coing) 1차 MVP 기획서](<https://github.com/prgrms-be-devcourse/NBE4-5-3-Team09/wiki/%EC%BD%94%EC%9E%89(Coing)-1%EC%B0%A8-MVP-%EA%B8%B0%ED%9A%8D%EC%84%9C>)

> (현재) [⭐️ 코잉(Coing) 2차 MVP 기획서](<https://github.com/prgrms-be-devcourse/NBE4-5-3-Team09/wiki/%EC%BD%94%EC%9E%89(Coing)-1%EC%B0%A8-MVP-%EA%B8%B0%ED%9A%8D%EC%84%9C>)

> [타겟 유저 시나리오](https://github.com/prgrms-be-devcourse/NBE4-5-3-Team09/wiki/Target-User-&-User-Scenario)

> [유저 플로우](https://github.com/prgrms-be-devcourse/NBE4-5-3-Team09/wiki/User-Flow)

> [와이어 프레임](docs/wireframes/) ([Creatie Link](https://creatie.ai/goto/IDpBmt9v?page_id=M&file=153513435570624))

## 🛠️ Technology Stack

> 자세한 내용은 [기술 스택 문서](https://github.com/prgrms-be-devcourse/NBE4-5-3-Team09/wiki/%EA%B8%B0%EC%88%A0-%EC%8A%A4%ED%83%9D)에서 확인 가능합니다.

#### 🎨 Frontend

<div align=""> 
  <img src="https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white"/>
  <img src="https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white"/>
  <img src="https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black"/>
  <img src="https://img.shields.io/badge/TypeScript-3178C6?style=for-the-badge&logo=typescript&logoColor=white"/>
  <img src="https://img.shields.io/badge/Next.js-000000?style=for-the-badge&logo=next.js&logoColor=white"/>
</div>

#### 🛠 Backend

<div align=""> 
  <img src="https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white"/>
  <img src="https://img.shields.io/badge/SpringBoot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"/>
  <img src="https://img.shields.io/badge/Apache%20Tomcat-F8DC75?style=for-the-badge&logo=apachetomcat&logoColor=white"/>
</div>

#### 🗄 Database

<div align=""> 
  <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white"/>
</div>

#### 🚀 Deployment & Infra

<div align=""> 
  <img src="https://img.shields.io/badge/Linux-FCC624?style=for-the-badge&logo=linux&logoColor=black"/>
  <img src="https://img.shields.io/badge/AWS-232F3E?style=for-the-badge&logo=amazonwebservices&logoColor=white"/>
  <img src="https://img.shields.io/badge/Vercel-000000?style=for-the-badge&logo=vercel&logoColor=white"/>
  <img src="https://img.shields.io/badge/Nginx-009639?style=for-the-badge&logo=nginx&logoColor=white"/>
  <img src="https://img.shields.io/badge/GitHub%20Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white"/>
</div>

## 🚀 System Architecture & Deployment

### Overview

![image](https://github.com/user-attachments/assets/1e110bb0-93fe-4577-ba1a-febb5f0355d1)

> 전체 요청 흐름 및 배포 자동화 파이프라인을 도식화한 구성도입니다.

### Deployment Structure

서비스는 **프론트엔드(Next.js)**와 **백엔드(Spring Boot with Kotlin)**로 구성되며,
각각 Vercel과 AWS EC2 + Docker 기반으로 배포됩니다.

- Frontend: Vercel을 통해 자동 배포

- Backend: GitHub Actions → Docker 이미지 생성 → ECR 업로드 → EC2에서 Pull 및 실행

- Database: Amazon RDS (MySQL)

- Infra Tooling: Doppler(환경 변수), Nginx(Proxy), Docker Compose, GitHub Actions

### CI/CD Process (GitHub Actions 기반 자동화)

1. GitHub Push

- main 브랜치에 푸시 시 자동으로 워크플로우 실행

2. 환경 변수 관리

- Doppler CLI로 .env 파일 자동 다운로드

3. 프론트엔드

- schema 변환, 타입 생성 → Vercel에 자동 배포

4. 백엔드

- 테스트 → Docker 이미지 빌드 → ECR에 Push → EC2에서 Pull 후 컨테이너 실행

> 🔗 자세한 자동화 및 배포 프로세스는 아래 GitHub Wiki에서 확인할 수 있습니다
> 👉 [배포 및 자동화 문서 바로가기](https://github.com/prgrms-be-devcourse/NBE4-5-3-Team09/wiki/%EB%B0%B0%ED%8F%AC-%EB%B0%8F-%EC%9E%90%EB%8F%99%ED%99%94)

## 🛠️ 개발 환경 설정 (Development Setup)

**1️⃣ Clone the Repository**

```bash
git clone https://github.com/prgrms-be-devcourse/NBE4-5-3-Team09.git
```

**2️⃣ Environment Variables (.env) Setup**
✅ Using Doppler (Recommended)

> Doppler는 .env 환경 변수 파일을 안전하게 관리해주는 도구입니다.

```bash
# Install Doppler CLI
brew install dopplerhq/cli/doppler

# Login & Setup
doppler login
doppler setup

# Run with environment loaded
npm run doppler
```

**3️⃣ Run Database (MySQL via Docker Compose)**

```bash
# Start MySQL container with Docker Compose
docker-compose up -d

# Monitor logs (logs are mapped locally)
tail -f ./mysql_logs/general.log

# Stop Containers
docker-compose down

```

**4️⃣ Run Backend (Spring Boot + Kotlin)**

```bash
cd backend

./gradlew bootRun
```

- Port: 8080

- Swagger Docs: http://localhost:8080/swagger-ui/index.html

**5️⃣ Run Frontend (Next.js)**

```bash
cd frontend

npm install  # Install dependencies (only needed once)
npm run dev  # Start development server

# Use OpenAPI to generate TypeScript types for the backend API
npm run codegen # Generate openapi typeScript definitions
npm run codegen:watch # Watch for API changes and regenerate types automatically

```

- Access: http://localhost:3000

## Project Structure

```coing/
├── backend/
│   └── src/main/kotlin/com/coing/
│       ├── domain/                       # 도메인 계층 (핵심 비즈니스 로직)
│       │   ├── bookmark/                 # 북마크 등록/조회/삭제
│       │   ├── chat/                     # 커뮤니티 채팅 기능
│       │   ├── coin/                     # 코인 관련 도메인 통합
│       │   │   ├── candle/               # 캔들 차트 데이터 (분봉, 일봉 등)
│       │   │   ├── common/               # 공통 인터페이스/포트/헬퍼
│       │   │   ├── market/               # 마켓 리스트, 마켓 정보 캐싱
│       │   │   ├── orderbook/            # 호가창 실시간 처리 및 전송
│       │   │   ├── ticker/               # 현재가 데이터 및 가격 변동률
│       │   │   └── trade/                # 체결 내역 관리
│       │   ├── news/                     # 코인 관련 뉴스 연동 (Naver API)
│       │   ├── notification/             # 실시간 알림 기능 (이벤트 기반)
│       │   └── user/                     # 회원 도메인 (인증, 권한 등)
│
│       ├── infra/                        # 인프라 계층 (외부 API, 메시징, WebSocket)
│       ├── global/                       # 전역 설정 (WebSocketConfig, Security 등)
│       └── Application.kt                # 스프링 부트 앱 실행 진입점
│
├── frontend/
│   ├── pages/                            # Next.js 페이지 (메인, 상세, 대시보드 등)
│   ├── components/                       # 공통 UI 컴포넌트
│   ├── schemas/                          # OpenAPI 기반 타입 정의 (자동 생성)
│   └── public/                           # 정적 파일
│
├── docker-compose.yml                   # MySQL 개발용 컨테이너 정의
├── .github/workflows/                   # GitHub Actions CI/CD 설정
├── .env.example                         # 환경 변수 예시
└── README.md                            # 프로젝트 문서

```

## 📄 API Docs & Data Schema

**📘 API∙Websocket 명세**

- [📃 API∙Websocket 명세](https://github.com/prgrms-be-devcourse/NBE4-5-3-Team09/wiki/API%E2%88%99Websocket-%EB%AA%85%EC%84%B8)
- Swagger UI:
  http://localhost:8080/swagger-ui/index.html

- OpenAPI 스키마 기반으로 프론트엔드에서 자동 타입 생성됨 (npm run codegen)

**📊 ERD (Entity Relationship Diagram)**

전체 테이블 구조 및 관계는 다음 링크에서 확인 가능합니다:

🔗 [코잉 ERD 문서 보기](https://github.com/prgrms-be-devcourse/NBE4-5-3-Team09/wiki/ERD)

## ❓ Etc

- [🛠️ TroubleShootings](https://github.com/prgrms-be-devcourse/NBE4-5-2-Team09/wiki/TroubleShootings)
- [📑 Project Convention](https://github.com/prgrms-be-devcourse/NBE4-5-2-Team09/wiki/Convention)
- [📝 Git Convention](https://github.com/prgrms-be-devcourse/NBE4-5-2-Team09/wiki/Git-Hub-%ED%98%91%EC%97%85-%EA%B7%9C%EC%B9%99)
