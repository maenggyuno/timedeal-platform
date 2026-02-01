# 🛠️ 기술 트러블슈팅 로그 (Troubleshooting Log)

이 문서는 프로젝트 구축 및 배포 과정에서 발생한 기술적 문제와 이를 해결하기 위한 시도 및 최종 해결 방안을 기록한 문서입니다.

## 📌 목차
### Part 1. 🚀 CI/CD 및 인프라 (Deployment & Infra)
> GitHub Actions, EC2, Docker 배포 과정에서 발생한 이슈
1. [GitHub Actions YAML 문법 에러 (들여쓰기)](#1-github-actions-yaml-문법-에러-들여쓰기)
2. [EC2 배포 시 Docker Compose 설정 파일 누락](#2-ec2-배포-시-docker-compose-설정-파일-누락)
3. [Private Repository 이미지 Pull 권한 거부](#3-private-repository-이미지-pull-권한-거부)
4. [저사양 EC2 인스턴스 배포 타임아웃 및 서버 프리징](#4-저사양-ec2-인스턴스-배포-타임아웃-및-서버-프리징)
5. [배포 환경에서의 환경 변수(.env) 동적 관리](#5-배포-환경에서의-환경-변수env-동적-관리)

### Part 2. 🔌 외부 API 연동 (External APIs)
> 지도(SGIS), 결제, 소셜 로그인 등 외부 서비스 연동 이슈
6. [SGIS API 302 Redirect 및 JSON 파싱 에러](#6-sgis-api-302-redirect-및-json-파싱-에러)

### Part 3. 🛡️ 보안 및 네트워크 (Security & Network)
> CORS, 인증/인가, 쿠키 세션 처리 이슈
7. [로컬 환경 CORS 인증 에러 (500 NPE & Credentials)](#7-로컬-환경-cors-인증-에러-500-npe--credentials)
8. [프론트엔드 요청 문법 오류로 인한 403 Forbidden](#8-프론트엔드-요청-문법-오류로-인한-403-forbidden)

---
---

## 1. GitHub Actions YAML 문법 에러 (들여쓰기)

### 🔍 문제 상황
- GitHub Actions 워크플로우 실행 중 `Invalid workflow file` 에러와 함께 특정 라인에서 문법 오류 발생.

### 🧐 원인 분석
- YAML 파일은 들여쓰기(Indentation)에 매우 엄격한 구조를 가짐.
- `script: |` 블록 하위의 셸 명령어들이 상위 계층인 `script`와 동일한 레벨에 위치하여 문법적으로 올바르지 않은 구조가 됨.

### ✨ 해결 방법
- 하위 명령어 블록에 스페이스 2~4칸을 추가하여 명확한 계층 구조를 형성함으로써 문법 오류 해결.

### 💡 배운 점
- 자동화 스크립트 작성 시 YAML 형식을 준수하는 것이 얼마나 중요한지 깨달았으며, IDE의 YAML 린트 기능을 활용하는 습관을 들임.

---

## 2. EC2 배포 시 Docker Compose 설정 파일 누락

### 🔍 문제 상황
- EC2 서버에서 `docker-compose pull` 실행 시 `no configuration file provided` 에러 발생하며 배포 중단.

### 🧐 원인 분석
- GitHub Actions가 도커 이미지는 레지스트리에 업로드하지만, 이를 실행하기 위한 설계도인 `docker-compose.yml` 파일은 EC2 서버로 전송하지 않았음.

### ✨ 해결 방법
- `appleboy/scp-action`을 워크플로우에 도입하여 배포 시점에 프로젝트 루트의 `docker-compose.yml`을 EC2의 배포 폴더로 자동 복사하도록 구성.

### 💡 배운 점
- 도커 이미지 배포와 컨테이너 오케스트레이션(설정 파일) 배포가 분리되어야 함을 이해하고, 전체적인 배포 자동화 흐름을 완성함.

---

## 3. Private Repository 이미지 Pull 권한 거부

### 🔍 문제 상황
- EC2 서버에서 최신 이미지를 받아오는 과정에서 `Access Denied` 에러 발생.

### 🧐 원인 분석
- 도커 허브 저장소를 보안을 위해 **Private**으로 설정했으나, EC2 서버가 해당 저장소에 접근하기 위한 인증 정보(Login)가 없는 상태에서 요청을 보냄.

### ✨ 해결 방법
- 배포 스크립트 최상단에 `echo "$DOCKERHUB_TOKEN" | docker login ...` 로직을 추가.
- 보안을 위해 비밀번호 대신 Docker Hub Access Token을 발행하고 GitHub Secrets를 통해 관리하도록 구현.

### 💡 배운 점
- 비공개 자산을 다룰 때의 인증 처리 메커니즘을 익혔으며, 민감 정보를 GitHub Secrets로 안전하게 관리하는 법을 습득함.

---

## 4. 저사양 EC2 인스턴스 배포 타임아웃 및 서버 프리징

### 🔍 문제 상황
- 배포 과정이 10분을 초과하며 타임아웃 발생 및 EC2 서버가 응답 불능 상태(Freezing)가 됨.

### 🧐 원인 분석
- 사용 중인 AWS `t2.micro` 인스턴스의 RAM(1GB) 사양 한계로 인해, 대용량 도커 이미지 압축 해제 시 CPU와 메모리 점유율이 100%에 도달함.

### ✨ 해결 방법
1.  **Swap Memory 설정:** EBS 공간의 일부를 가상 메모리(2GB)로 할당하여 메모리 부족 현상 완화. (비용 추가 없이 서버 안정성 확보)
2.  **Timeout 연장:** `ssh-action`의 `command_timeout` 설정을 30분으로 늘려 일시적인 부하 상황에서도 배포가 끝까지 진행되도록 보장.

### 💡 배운 점
- 클라우드 인프라의 자원 제약 상황을 이해하고, 리눅스 시스템 설정을 통해 하드웨어 한계를 극복하는 트러블슈팅 경험을 쌓음.

---

## 5. 배포 환경에서의 환경 변수(.env) 동적 관리

### 🔍 문제 상황
- 컨테이너는 정상 실행되나 DB 접속 정보나 API Key를 찾지 못해 애플리케이션 에러 발생.

### 🧐 원인 분석
- 보안상 `.env` 파일을 리포지토리에 올리지 않았기 때문에, 배포된 EC2 서버 내부에는 실행에 필요한 환경 변수 파일이 존재하지 않음.

### ✨ 해결 방법
- GitHub Secrets에 모든 환경 변수를 등록하고, 배포 시점에 워크플로우가 EC2 내부에서 `touch .env` 및 `echo` 명령어를 통해 실시간으로 `.env` 파일을 생성하도록 자동화.

### 💡 배운 점
- CI/CD 파이프라인에서 보안과 운영 편의성을 동시에 잡는 환경 변수 관리 전략을 수립함.

---

## 6. SGIS API 302 Redirect 및 JSON 파싱 에러
### 🛑 문제 상황 (Problem)
- 백엔드에서 SGIS 토큰 발급 요청 시 `com.fasterxml.jackson.core.JsonParseException` 발생.
- 로그 분석 결과, 응답 본문이 JSON이 아닌 HTML(302 Found, Redirect)로 리턴됨.

### 🔍 원인 (Cause)
- SGIS API의 서비스 도메인이 변경되었음 (`sgisapi.kostat.go.kr` → `sgisapi.mods.go.kr`).
- 기존 코드는 구 도메인으로 요청을 보내고 있었고, 서버가 리다이렉트 응답을 주자 자바가 이를 JSON으로 파싱하려다 실패함.

### 💡 해결 (Solution)
- `application.properties` 및 `SgisService`의 Base URL을 변경된 도메인(`mods.go.kr`)으로 업데이트하여 해결.

---

## 7. 로컬 환경 CORS 인증 에러 (500 NPE & Credentials)
### 🛑 문제 상황 (Problem)
- 매장 생성 API 호출 시 `500 Internal Server Error` 발생.
- 백엔드 로그: `java.lang.NullPointerException: Cannot invoke "UserPrincipal.getUserId()" because "user" is null`.

### 🔍 원인 (Cause)
- 로컬 개발 환경(`localhost:3000` -> `localhost:8080`) 간의 통신에서 브라우저의 **Same-Site 정책**으로 인해 쿠키(인증 토큰)가 전송되지 않음.
- 백엔드의 `JwtAuthenticationFilter`가 토큰을 찾지 못해 사용자를 `Anonymous`로 처리했고, 컨트롤러에서 유저 정보를 꺼낼 때 NPE 발생.

### 💡 해결 (Solution)
- 프론트엔드 `fetch` 옵션에 `credentials: 'include'`를 추가.
- 브라우저가 Cross-Origin 요청(로컬 포트 차이)에서도 명시적으로 쿠키를 포함하여 요청을 보내도록 설정.

---

## 8. 프론트엔드 요청 문법 오류로 인한 403 Forbidden
### 🛑 문제 상황 (Problem)
- 매장 등록 버튼 클릭 시 `403 Forbidden` 에러 발생.
- 브라우저 콘솔 로그: `POST http://localhost:3000/.../${baseUrl}/...`

### 🔍 원인 (Cause)
- 자바스크립트 `fetch` 함수 내에서 템플릿 리터럴 사용 시 백틱(`` ` ``) 대신 작은따옴표(`'`)를 사용함.
- `${baseUrl}` 변수가 치환되지 않고 문자열 그대로 URL에 포함되어 잘못된 경로로 요청됨.

### 💡 해결 (Solution)
- URL 선언부를 작은따옴표(`'`)에서 백틱(`` ` ``)으로 수정하여 변수 인터폴레이션(Interpolation)이 정상 작동하도록 조치.
