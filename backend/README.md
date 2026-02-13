백엔드(Backend) README.md (기술 문서)

## 🚀 EC2 배포 가이드 (Ubuntu 22.04 / t3.micro 기준)

서버를 처음 세팅하거나 재배포할 때 아래 명령어를 순서대로 실행하세요.

### 1. 기본 환경 설정 및 Java 21 설치
```bash
# 패키지 목록 업데이트
sudo apt update

# OpenJDK 21 설치
sudo apt install openjdk-21-jdk -y

# 설치 확인
java -version
```

### 2. 스왑 메모리 설정 (필수)
AWS 프리티어(t2.micro, t3.micro)는 RAM이 1GB라 빌드 중 서버가 멈출 수 있습니다. 이를 방지하기 위해 가상 메모리를 설정합니다.

```bash
# 2GB 스왑 파일 생성
sudo dd if=/dev/zero of=/swapfile bs=128M count=16

# 권한 설정
sudo chmod 600 /swapfile

# 스왑 영역 설정 및 활성화
sudo mkswap /swapfile
sudo swapon /swapfile

# 설정 확인 (Swap: 2.0Gi 확인)
free -h
```

### 3. 프로젝트 설치 및 빌드
```bash
# 1. 깃허브에서 프로젝트 가져오기
git clone https://github.com/maenggyuno/timedeal-platform.git

# 2. 백엔드 디렉토리로 이동
cd timedeal-platform/backend

# 3. gradlew 실행 권한 부여
chmod +x gradlew

# 4. 프로젝트 빌드 (테스트 제외)
# DB가 연결되지 않은 상태이므로 테스트(-x test)를 건너뜁니다.
./gradlew build -x test
```

### 4. 서버 실행
빌드된 jar 파일을 실행합니다. (`-plain`이 붙지 않은 파일)

```bash
java -jar build/libs/timedeal-platform-backend-0.0.1-SNAPSHOT.jar
```


## 💾 Database Setup (RDS) & Best Practices

이 가이드는 AWS RDS(MySQL) 설정 방법과 운영 환경(Linux) 배포 시 반드시 지켜야 할 데이터베이스 규칙을 다룹니다.

### 1. RDS 초기 세팅 (필수 명령어)
로컬(내 컴퓨터)이 아닌 **RDS(서버 컴퓨터)**에는 데이터베이스 방(Schema)이 없습니다. 반드시 접속 후 아래 명령어를 실행해야 합니다.

```sql
-- 1. 데이터베이스 생성 (이름: timedeal_db)
-- 주의: 로컬과 동일한 이름을 사용하는 것이 정신 건강에 좋습니다.
CREATE DATABASE timedeal_db;

-- 2. 사용 설정 (이걸 안 하면 "No database selected" 에러 발생)
USE timedeal_db;

-- 3. 테이블 확인 (스프링 부트 실행 후 생성되었는지 확인)
SHOW TABLES;
-- 생성된 이름이 'order_items'(Snake Case)인지 'order_items'(Camel Case)인지 반드시 체크!
```

---

### 2. 네이밍 컨벤션 (Naming Convention) - ⭐ 가장 중요!
윈도우(Local)와 리눅스(RDS/EC2)의 파일 시스템 차이로 인해 **대소문자 구분 이슈**가 발생합니다. 아래 표준을 반드시 따르세요.

* **Java (Code):** CamelCase (`order_items`)
* **Database (SQL):** SnakeCase (`order_items`)

**[해결 방법]**

1.  **Entity 클래스:** `@Table` 어노테이션으로 명시적 매핑
    ```java
    @Entity
    @Table(name = "order_items") // DB에는 무조건 이렇게 만들어라!
    public class order_items { ... }
    ```

2.  **Native Query (`@Query`):** 실제 DB 테이블 이름 사용
    ```java
    // (X) SELECT * FROM order_items ... (리눅스에서 에러 남)
    // (O) SELECT * FROM order_items ... (정상 동작)
    @Query(value = "SELECT * FROM order_items ...", nativeQuery = true)
    ```

---

### 3. application.properties 설정 (RDS 연결)
URL 작성 시 **RDS 주소(건물)**와 **DB 이름(방 번호)**을 혼동하지 않도록 주의하세요.

```properties
# 올바른 형식
# jdbc:mysql://[RDS엔드포인트]:3306/[만든_DB이름]
spring.datasource.url=jdbc:mysql://[timedeal-platform-db.xxx.ap-northeast-2.rds.amazonaws.com:3306/timedeal_db?serverTimezone=Asia/Seoul&characterEncoding=UTF-8](https://timedeal-platform-db.xxx.ap-northeast-2.rds.amazonaws.com:3306/timedeal_db?serverTimezone=Asia/Seoul&characterEncoding=UTF-8)
```


### 3. 환경 설정 (Configuration)
   보안을 위해 민감한 정보는 깃허브에 올리지 않고, 서버에서 직접 파일을 생성하여 관리합니다.

3-1. application.properties 설정 (EC2)
서버의 src/main/resources/ 위치가 아닌, Jar 파일이 실행되는 위치에 별도로 작성합니다.

```properties
# ==================================
# 1. 데이터베이스 설정 (RDS 연결)
# ==================================
# # 주의: URL 뒤에 DB이름(timedeal_db)과 옵션을 정확히 명시해야 함
spring.datasource.url=jdbc:mysql://timedeal-platform-db.crsiwosc4szo.ap-northeast-2.rds.amazonaws.com:3306/timedeal_db?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
spring.datasource.username=admin
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA 테이블 자동 생성 (서버 켜질 때 테이블 생성됨)
spring.jpa.hibernate.ddl-auto=update
# 1. 프로필 & 파일 가져오기 (가장 중요!)
spring.profiles.active=default
spring.config.import=file:./application-login.properties
# ==================================
# 2. 서버 전용 설정
# ==================================

# [중요] 프론트엔드 주소 설정 (CORS)
# 지금은 리액트를 '내 컴퓨터(로컬)'에서 켜서 테스트하실 거죠?
# 그렇다면 아래처럼 localhost:3000을 적어줘야 에러가 안 납니다.
# 배포 서버에서 킬 때는 http://:3.36.179.239:3000으로 변경해야함. 
# #이후 도메인 적용시에 또 다시 변경해야함
frontend.url=http://localhost:3000


# 쿠키 보안 (HTTPS 적용 전이라 false)
cookie.secure=false

# ==================================
# 3. 로그 및 성능 최적화 (배포 환경용)
# ==================================
# 불필요한 로그를 줄여서 서버 멈춤 방지
logging.level.org.springframework.security=INFO
logging.level.org.springframework.web.filter=INFO
logging.level.io.jsonwebtoken=INFO
logging.level.org.springframework.security.oauth2=INFO

# SQL 실행 로그 끄기 (속도 향상)
spring.jpa.properties.hibernate.show_sql=false
spring.jpa.properties.hibernate.format_sql=false


#s3는 키 바꿔야 함
cloud.aws.s3.bucket=
cloud.aws.s3.region=ap-northeast-2
cloud.aws.credentials.access-key=
cloud.aws.credentials.secret-key=

# Toss Payments 변경해야함 (Secret Key)
toss.payments.key=

# Business Number 변경해야함
api.gonggong.serviceKey=

# Naver Maps api  url 설정해야함
naver.client-id=
naver.secret=
```

3-2. application-login.properties 설정 (OAuth)

```properties
# Google Login
spring.security.oauth2.client.registration.google.client-name=google
spring.security.oauth2.client.registration.google.client-id=
spring.security.oauth2.client.registration.google.client-secret=
spring.security.oauth2.client.registration.google.redirect-uri=http://3.36.179.239:8080/login/oauth2/code/google
spring.security.oauth2.client.registration.google.authorization-grant-type=authorization_code
spring.security.oauth2.client.registration.google.scope=profile, email

# Naver Login
#[중요] 네이버 로그인 api 주소설정 배포시에는 서버 application-login.properties 파일에는 3.36.179.239:8080 고정 ip 할당
#[중요] 도메인 적용시에는 적용된 도메인 다시 변경해야함
#[중요] 다만 로컬 application-login.properties 파일에는 localhost:8080으로 설정
spring.security.oauth2.client.registration.naver.client-name=naver
spring.security.oauth2.client.registration.naver.client-id=
spring.security.oauth2.client.registration.naver.client-secret=
spring.security.oauth2.client.registration.naver.redirect-uri=http://3.36.179.239:8080/login/oauth2/code/naver
spring.security.oauth2.client.registration.naver.authorization-grant-type=authorization_code
spring.security.oauth2.client.registration.naver.scope=name, email
spring.security.oauth2.client.provider.naver.authorization-uri=https://nid.naver.com/oauth2.0/authorize
spring.security.oauth2.client.provider.naver.token-uri=https://nid.naver.com/oauth2.0/token
spring.security.oauth2.client.provider.naver.user-info-uri=https://openapi.naver.com/v1/nid/me
spring.security.oauth2.client.provider.naver.user-name-attribute=response

# JWT
jwt.secret = 
jwt.expiration.ms=3600000
jwt.cookie.name=access_token
# JWT refresh token - ?? ?? ?, access token? ????? ???? ?? ??? ? ??
jwt.refresh.expiration.ms=604800000
```

### 4. 빌드 및 실행 (Build & Run)

#### 4-1. 서버 업데이트 (EC2 Server)
서버에 접속하여 최신 코드를 가져오고 재빌드합니다.

1. **프로젝트 폴더로 이동**
```bash
cd ~/timedeal-platform/backend
```
2. **최신 코드 가져오기 (Git Pull)**
```bash
git pull origin main
```
Username: 깃허브 아이디 입력

Password: 깃허브 Personal Access Token(PAT) 입력 (일반 비밀번호는 보안상 거부됩니다.)


#### 4-2. 빌드 전 기존 파일 제거 (Clean Build)
빌드 시 이전에 생성된 파일이 충돌을 일으킬 수 있으므로, 기존 `build` 폴더를 삭제하고 새롭게 빌드하는 것을 권장합니다.

```bash
# 1. 기존 빌드 폴더 완전 삭제 (강력 권장)
rm -rf build

# 2. 프로젝트 빌드 (테스트 제외 시 -x test 옵션 추가)
sudo ./gradlew clean build -x test
```

```bash
# 스왑 메모리 활성화
sudo swapon /swapfile

# 잘 켜졌는지 확인 (Swap 부분에 숫자가 보여야 함)
free -h
```
`
#### 4-3. 서버 실행
```bash
java -jar build/libs/timedeal-platform-backend-0.0.1-SNAPSHOT.jar
```


# 📑 AWS S3 & IAM Infrastructure Setup Guide

본 가이드는 `timedeal-platform` 프로젝트의 확장성과 보안을 위해 **환경별 버킷 분리(Dev/Prod)** 및 **IAM 최소 권한 원칙**을 적용한 설정 과정을 기록합니다.

---

### 1. IAM: 권한 격리 및 계정 생성
관리자(`admin`) 계정의 액세스 키 노출 위험을 방지하기 위해 S3 전용 그룹과 사용자를 생성하여 운영합니다.

* **IAM 그룹 생성 및 정책 연결**
    * **Group Name**: `s3-group`
    * **Policy**: `AmazonS3FullAccess` 직접 연결
* **IAM 사용자 생성 및 할당**
    * **User Name**: `s3-user1`
    * **Group**: `s3-group` (생성한 그룹 선택)
* **액세스 키 발급**
    * 해당 사용자 상세 페이지 > **보안 자격 증명** > **액세스 키 만들기**
    * **Access Key ID** & **Secret Access Key** 보관 (환경 변수 등록용)

---

### 2. S3: 버킷 구축 및 보안 설정
데이터 정합성을 위해 개발과 운영 버킷을 물리적으로 분리하고 브라우저 직업로드를 위한 보안 설정을 적용합니다.

* **버킷 생성**
    * `timedeal-platform-dev-s3-bucket` (로컬 개발용)
    * `timedeal-platform-prod-s3-bucket` (서버 배포용)
* **CORS 정책 (브라우저 Presigned URL 직업로드 허용)**
    ```json
    [
      {
        "AllowedHeaders": ["*"],
        "AllowedMethods": ["GET", "PUT", "POST", "HEAD"],
        "AllowedOrigins": ["http://localhost:3000", "[https://your-domain.com](https://your-domain.com)"],
        "ExposeHeaders": ["ETag"],
        "MaxAgeSeconds": 3000
      }
    ]
    ```
* **퍼블릭 읽기 권한 (이미지 UI 노출 허용)**
    * **퍼블릭 액세스 차단**: '모든 퍼블릭 액세스 차단' 체크 해제
    * **버킷 정책(Bucket Policy)**:
    ```json
    {
      "Version": "2012-10-17",
      "Statement": [{
        "Sid": "PublicReadGetObject",
        "Effect": "Allow",
        "Principal": "*",
        "Action": "s3:GetObject",
        "Resource": "arn:aws:s3:::본인-버킷-명칭/*"
      }]
    }
    ```

---

### 3. Application: 환경별 프로파일 설정
소스 코드 내 기민 정보 노출을 방지하기 위해 환경 변수 주입 방식을 사용합니다.

* **application-dev.properties (로컬 개발)**
    ```properties
    cloud.aws.s3.bucket=timedeal-platform-dev-s3-bucket
    cloud.aws.region.static=ap-northeast-2
    cloud.aws.credentials.access-key=${AWS_ACCESS_KEY}
    cloud.aws.credentials.secret-key=${AWS_SECRET_KEY}
    ```
* **application-prod.properties (서버 배포)**
    ```properties
    cloud.aws.s3.bucket=timedeal-platform-prod-s3-bucket
    cloud.aws.region.static=ap-northeast-2
    cloud.aws.credentials.access-key=${AWS_ACCESS_KEY}
    cloud.aws.credentials.secret-key=${AWS_SECRET_KEY}
    ```

---

### 4. Deployment: 실행 명령어
배포 시점에 적절한 프로파일을 활성화하여 인프라 설정을 동적으로 주입합니다.

* **Docker 실행 시 (환경 변수 주입)**
    ```bash
    docker run -d \
      -e SPRING_PROFILES_ACTIVE=prod \
      -e AWS_ACCESS_KEY=발급받은_키 \
      -e AWS_SECRET_KEY=발급받은_비밀키 \
      --name app-container image-name
    ```
* **JAR 직접 실행 시**
    ```bash
    java -jar -Dspring.profiles.active=prod app.jar
    ```

## 🚀 CI/CD 및 배포 인프라 (Deployment Architecture)

이 프로젝트는 **GitHub Actions**, **Docker**, **AWS EC2**를 활용하여 **자동화된 배포 파이프라인(CI/CD)**을 구축했습니다.
로컬 개발 환경과 운영 환경(Production)의 차이를 이해하고, 보안을 고려하여 설계되었습니다.

### 1. 전체 배포 흐름 (CI/CD Pipeline Flow)

개발자가 코드를 `main` 브랜치에 `push`하면 다음과 같은 순서로 배포가 진행됩니다.

**GitHub Actions (Runner)**
1.  **Checkout:** 최신 소스 코드를 가져옵니다.
2.  **Build:** `Dockerfile`을 기반으로 Spring Boot(백엔드)와 React(프론트엔드) 이미지를 빌드합니다.
3.  **Login & Push:** Docker Hub(Private Repo)에 로그인 후, 빌드된 이미지를 업로드합니다.
4.  **Connect EC2:** SSH Key를 이용해 AWS EC2 서버에 접속합니다.
5.  **Copy Config:** `docker-compose.yml` (실행 설계도) 파일을 EC2로 전송(`scp`)합니다.
6.  **Deploy:**
    * EC2 내부에서 Private Docker Hub에 로그인합니다.
    * GitHub Secrets를 기반으로 `.env` 파일을 동적으로 생성합니다. (보안 유지)
    * `docker-compose pull`로 최신 이미지를 다운로드합니다.
    * `docker-compose up -d`로 컨테이너를 재실행(무중단 배포 지향)합니다.
    * `docker image prune`으로 불필요한 구버전 이미지를 정리합니다.

### 2. 환경별 동작 방식 비교

| 구분 | 로컬 개발 환경 (Local) | 운영 배포 환경 (Production EC2) |
| :--- | :--- | :--- |
| **실행 주체** | 개발자 PC (내 컴퓨터) | AWS EC2 (클라우드 서버) |
| **실행 도구** | Docker Desktop | Docker Engine (Linux) |
| **빌드 방식** | `docker-compose up --build`로 즉시 빌드 및 실행 | GitHub Actions가 빌드 후 '이미지'만 전달 |
| **파일 관리** | 소스 코드 전체가 로컬에 존재 | **소스 코드 없음**, 오직 `docker-compose.yml`과 `.env`만 존재 |
| **네트워크** | `localhost:3000` ↔ `localhost:8080` | `EC2_IP:80`(Nginx) ↔ `timedeal-backend:8080`(Docker Network) |

### 3. 주요 파일 및 기술 설명

* **Dockerfile:**
    * 애플리케이션을 실행하기 위한 운영체제(JDK, Node.js)와 설정이 담긴 '요리 레시피'입니다.
    * 백엔드는 `open-jdk-17`을 사용하여 JAR 파일을 실행하고, 프론트엔드는 `nginx`를 사용하여 정적 파일을 서빙합니다.

* **docker-compose.yml:**
    * 백엔드, 프론트엔드, DB 등 여러 컨테이너를 한 번에 관리하는 '통합 설계도'입니다.
    * 배포 시 GitHub Actions가 이 파일을 EC2로 복사해주기 때문에, EC2에는 소스 코드 없이 이 파일 하나만 있으면 서버가 돌아갑니다.

* **.github/workflows/\*.yml:**
    * CI/CD 로봇에게 내리는 명령서입니다.
    * `paths` 필터를 사용하여 백엔드 코드가 수정되면 백엔드만, 프론트엔드가 수정되면 프론트엔드만 효율적으로 배포합니다.

### 4. 보안 및 Private Repository 전략

* **Private Image:**
    * 상용 서비스 수준의 보안을 위해 Docker Hub 리포지토리를 **Private**으로 설정했습니다.
    * 배포 스크립트(`deploy.yml`) 내에 `docker login` 로직을 추가하여 권한이 있는 서버만 이미지를 받을 수 있게 했습니다.

* **Environment Variables (.env):**
    * DB 비밀번호, API Key 등 민감 정보는 코드에 포함하지 않습니다(`gitignore`).
    * **프론트엔드:** 빌드 시점에 환경변수가 주입되어 이미지에 포함됩니다. (보안 취약점 방지를 위해 주요 Key는 백엔드로 위임 예정)
    * **백엔드:** 배포 시점에 EC2 내부에서 `.env` 파일을 생성하여 컨테이너 실행 시 주입합니다.

### 5. EC2 주요 명령어 (Maintenance)

서버 유지보수를 위해 자주 사용하는 명령어입니다.

```bash
# 1. 서버 접속
ssh -i "key.pem" ubuntu@EC2_IP

# 2. 현재 실행 중인 컨테이너 확인
docker ps

# 3. 죽은 컨테이너 로그 확인 (에러 추적)
docker logs <컨테이너ID>

# 4. 강제 재배포 (캐시 문제 등 발생 시)
# 이미지를 지우고 다시 받아옵니다.
docker-compose down
docker system prune -a -f
docker-compose pull
docker-compose up -d

# 5. 디스크 용량 확인 및 스왑 메모리 확인
df -h      # 디스크
free -h    # 메모리 (Swap 확인)
```

### 🐳 Docker 실시간 로그 확인 명령어

배포 환경(EC2)에서 백엔드 서버의 에러를 추적하거나 상태를 모니터링할 때 사용하는 주요 명령어입니다.

1. **실시간 로그 전체 스트리밍**
   > 컨테이너에서 발생하는 모든 로그를 실시간으로 계속 출력합니다.
   ```bash
   docker logs -f [컨테이너_ID_또는_이름]
   ```

2. **최근 로그 요약 및 실시간 추적 (추천)**
   > 최근 100줄의 로그만 먼저 보여준 뒤, 새로 발생하는 로그를 실시간으로 표시합니다. (화면 스크롤 압박이 적어 효율적입니다.)
   ```bash
   docker logs -f --tail 100 [컨테이너_ID_또는_이름]
   ```

3. **로그 내 특정 키워드 검색 (에러 확인용)**
   > 로그 전체 내용 중 'Exception'이나 'Error' 같은 특정 단어가 포함된 줄만 찾아서 보여줍니다.
   ```bash
   docker logs [컨테이너_ID_또는_이름] | grep "Exception"
   ```

## 🐳 도커 컨테이너 내부 DB 확인 방법 (MySQL)

로컬 개발 환경에서 도커 컨테이너(`timedeal-local-db`)에 접속하여 테이블 생성 및 컬럼 변경 사항을 확인하는 방법입니다.

### 1. MySQL 접속
Docker Desktop의 **Containers > Exec** 탭(또는 터미널)에서 아래 명령어를 입력합니다.

```bash
# MySQL 접속 (root 계정)
mysql -u root -p

# Enter password: 문구가 나오면 설정한 비밀번호 입력
# (보안상 입력 시 화면에는 아무것도 표시되지 않습니다. 입력 후 Enter를 누르세요.)
```

### 2. 데이터베이스 및 테이블 확인 (SQL)
MySQL에 접속한 상태(`mysql>`)에서 아래 쿼리문을 순서대로 실행하여 구조를 확인합니다.

```sql
-- 1. 데이터베이스 목록 조회
show databases;

-- 2. 프로젝트 DB 선택 (본인의 DB명으로 변경, 예: timedeal_db)
use [DB이름];

-- 3. 전체 테이블 목록 조회
-- (reviews, carts, follows 테이블이 생성되었는지 확인)
show tables;

-- 4. 주요 변경사항 상세 확인
-- 4-1. order_items 테이블에 total_price 컬럼이 추가되었는지 확인
desc order_items;

-- 4-2. 신규 테이블(reviews) 구조 확인
desc reviews;
```

### 💡 팁 (Tip)
* `desc [테이블명];` 명령어를 사용하면 해당 테이블의 컬럼명(Field), 타입(Type), Null 여부, Key(PK/FK) 정보를 자세히 볼 수 있습니다.
* 도커 컨테이너를 재시작 
* ```bash
  docker-compose -f docker-compose-local.yml up -d  
  ```
* 코드 변경 후 이미지 다시 굽고 시작 
* ```bash
   docker-compose -f docker-compose-local.yml up -d --build
  ```
###  6. 트러블 슈팅 가이드 (Memo)
배포 중 Timeout 발생 시: EC2 사양 문제일 수 있으므로 Swap Memory가 활성화되어 있는지 확인한다.

"No configuration file provided" 에러: docker-compose.yml 파일이 EC2 경로에 없는 것이다. CI/CD의 scp 단계를 확인한다.

.env 파일이 숨겨지므로 ls-al 명령어를 치면 .env 파일이 생성된 것을 확인할 수 있다.

# 🌐 Cloudflare DNS 설정: 프론트엔드 연결 및 회색 구름 전환

메인 도메인은 사용자가 접속하는 얼굴이므로 **AWS CloudFront**에 양보하고, 기존 백엔드 터널은 **`api` 서브도메인**으로 옮겨주는 작업입니다.

### 1. Cloudflare DNS 메뉴 접속
1. Cloudflare 메인 화면에서 활성화된 **`dongnekok.shop`** 사이트를 클릭합니다.
2. 왼쪽 사이드바 메뉴에서 **[DNS] -> [Records]**로 들어갑니다.

### 2. 프론트엔드(CloudFront) 레코드 추가
이미 터널용 레코드가 있다면 해당 레코드를 수정하거나 삭제 후 새로 만드세요.

* **루트 도메인 설정 (`@`):**
    * **Type:** `CNAME`
    * **Name:** `@` (또는 `dongnekok.shop` 직접 입력)
    * **Target:** **AWS CloudFront 주소** (예: `d1234abcd.cloudfront.net`)
    * **Proxy Status:** 주황색 구름을 클릭하여 **회색 구름(DNS Only)**으로 변경 ☁️
* **www 도메인 설정 (`www`):**
    * **Type:** `CNAME`
    * **Name:** `www`
    * **Target:** 위와 동일한 CloudFront 주소
    * **Proxy Status:** **회색 구름(DNS Only)**으로 변경 ☁️

### 3. 백엔드(api) 서브도메인 등록 (터널용)
백엔드 서버 접속을 위해 `api` 주소를 터널에 할당해야 합니다.

* **Type:** `CNAME`
* **Name:** `api`
* **Target:** `<터널ID>.cfargotunnel.com` (또는 터널 생성 시 할당된 주소)
* **Proxy Status:** **주황색 구름(Proxied)** 권장 (보안 및 DDoS 방어용)

---

## ⚠️ 주의사항: 백엔드 터널 설정 변경

DNS에서 `api` 주소를 만들었다면, **EC2 내부의 `config.yml`** 파일도 함께 수정해줘야 백엔드가 정상 작동합니다.

1. **설정 파일 열기:** `sudo nano /etc/cloudflared/config.yml`
2. **내용 수정:**
   ```yaml
   ingress:
     - hostname: api.dongnekok.shop  # 기존 메인 도메인에서 api로 변경
       service: http://localhost:8080
     - service: http_status:404
   ```
3. **재시작:** `sudo systemctl restart cloudflared`

---

## 💡 왜 회색 구름(DNS Only)을 쓰나요?
* **SSL 인증서 충돌 방지:** AWS CloudFront에서 이미 SSL(ACM)을 사용 중인 경우, Cloudflare의 프록시(주황 구름)와 설정이 꼬여 "Too many redirects" 에러가 날 수 있습니다.
* **속도:** DNS 해석만 Cloudflare가 담당하고, 실제 데이터 전송은 AWS 인프라를 직접 타게 되어 더 깔끔한 연결이 가능합니다.



## 🚇 Infrastructure: Cloudflare Tunnel Setup

보안 강화를 위해 외부에서 EC2의 포트(8080)를 직접 열지 않고, **Cloudflare Tunnel**을 통해 안전하게 연결했습니다.
Nginx를 사용하지 않고도 HTTPS(SSL)가 자동 적용되며, 실제 서버 IP를 숨길 수 있는 아키텍처입니다.

### 1. Architecture
* **Host (EC2 Ubuntu):** `cloudflared` 데몬이 실행되어 외부 트래픽을 수신.
* **Container (Docker):** Spring Boot 애플리케이션이 `8080` 포트로 실행 중.
* **Connection:** Tunnel이 `localhost:8080`으로 트래픽을 포워딩.

### 2. Installation (Ubuntu)
EC2 호스트(Root) 환경에 직접 설치하여 Docker 컨테이너와의 의존성을 분리했습니다.

```bash
# 1. 설치 파일 다운로드 (.deb)
wget -q https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-amd64.deb

# 2. 패키지 설치
sudo dpkg -i cloudflared-linux-amd64.deb
```

### 3. Authentication & Setup
Cloudflare 계정과 서버를 연동하는 과정입니다.

```bash
# 1. 로그인 (URL을 로컬 브라우저에 복사하여 인증)
cloudflared tunnel login

# 2. 터널 생성 (이름: shop) -> UUID 생성됨
cloudflared tunnel create shop

# 3. DNS 라우팅 연결 (CNAME 자동 설정)
cloudflared tunnel route dns shop dongnekok.shop
```

### 4. Configuration (`config.yml`)
터널이 트래픽을 어디로 보낼지 정의합니다.
* **Path:** `/home/ubuntu/.cloudflared/config.yml`

```yaml
tunnel: <Generated-UUID>
credentials-file: /home/ubuntu/.cloudflared/<Generated-UUID>.json

ingress:
  # 1. 메인 트래픽 -> 로컬 도커 컨테이너로 전달
  - hostname: dongnekok.shop
    service: http://localhost:8080

  # 2. 정의되지 않은 규칙은 404 처리
  - service: http_status:404
```

### 5. Execution
터널을 실행하여 외부 접속을 활성화합니다.

```bash
cloudflared tunnel run shop
```


# 🚀 Cloudflare Tunnel 자동화 및 서버 안정화 가이드 (Systemd Service)

단순 명령어 실행(`run`) 방식에서 벗어나, 서버 재부팅 시에도 자동으로 터널이 복구되는 **시스템 서비스(Systemd)** 등록 과정을 정리합니다. 이를 통해 'TimeDeal' 플랫폼의 24시간 무중단 운영 환경을 구축했습니다.

---

## 1. 개요: 왜 서비스 등록이 필요한가?
* **지속성**: 터미널 세션이 종료되어도 백그라운드에서 터널이 상주합니다.
* **자동 복구**: 서버 에러나 재부팅 발생 시 시스템이 자동으로 터널 프로세스를 다시 실행합니다.
* **전문성**: 수동 실행보다 운영 안정성이 높아 실제 배포 환경에 적합한 정석적인 방식입니다.

---

## 2. 터널 서비스 자동화 절차

`cloudflared`는 보안 및 시스템 권한 관리를 위해 `/etc/cloudflared` 경로를 기본 설정 저장소로 사용합니다.

### ① 시스템 설정 폴더 생성 및 파일 이관
사용자 홈 디렉토리에 있는 설정 파일들을 시스템 공용 폴더로 복사합니다.
```bash
# 시스템 설정 폴더 생성
sudo mkdir -p /etc/cloudflared

# 기존 설정 파일(.yml) 및 인증 키(.json) 복사
sudo cp ~/.cloudflared/config.yml /etc/cloudflared/
sudo cp ~/.cloudflared/*.json /etc/cloudflared/
```

### ② 터널 서비스(Unit) 설치
시스템이 터널을 서비스로 인식하도록 설치 명령을 실행합니다.
```bash
# 시스템 서비스로 정식 등록
sudo cloudflared service install
```

### ③ 서비스 활성화 및 상태 관리
```bash
# 서비스 시작 및 부팅 시 자동 시작 설정
sudo systemctl start cloudflared
sudo systemctl enable cloudflared

# 실행 상태 확인 (Active: active (running) 확인 필수)
sudo systemctl status cloudflared
```

### 6. Verification (Troubleshooting)
배포 초기 접근 시 아래와 같은 에러 코드를 통해 정상 작동을 확인했습니다.

* **Status 404 (Whitelabel Error Page):** Spring Boot 서버가 정상적으로 실행되어 응답함. (메인 루트 경로 매핑 부재)
* **Status 401 (Unauthorized):** Spring Security가 정상 작동하여 인증되지 않은 요청을 차단함.
* **Result:** 인프라 연결 및 백엔드 보안 설정이 정상적으로 완료됨을 확인.

## 🌿 Git 브랜치 생성 및 전환 명령어

### 1. 가장 많이 사용하는 명령어 (전통적 방식)
브랜치를 생성함과 동시에 해당 브랜치로 바로 이동합니다.
```bash
git checkout -b [브랜치-이름]
```
> **예시:** `git checkout -b feat/api-connection-test`

### 2. 최신 권장 방식 (Git 2.23 이상)
`checkout` 명령어가 기능이 너무 많아 분리된 최신 명령어입니다. 가독성이 더 좋아 권장됩니다.
```bash
git switch -c [브랜치-이름]
```
* `-c`는 'create'의 약자로, 브랜치를 만들면서(create) 동시에 이동(switch)하겠다는 뜻입니다.

---

### 3. 브랜치 이름 추천 (관례)
졸업 작품이나 실제 협업에서는 보통 아래와 같은 규칙(Prefix)을 붙여서 이름을 만듭니다.

| 접두어(Prefix) | 용도 | 예시 |
| :--- | :--- | :--- |
| **`feat/`** | 새로운 기능 추가 시 | `feat/api-test-controller` |
| **`fix/`** | 버그나 오류 수정 시 | `fix/cors-policy` |
| **`env/`** | 환경 변수나 설정 파일 변경 시 | `env/add-env-file` |
| **`refactor/`** | 코드 리팩토링 시 | `refactor/api-call-logic` |

---

### 4. 브랜치 생성 후 작업 흐름 (Quick Sheet)
1. **브랜치 생성 및 이동**: `git switch -c feat/test-api`
2. **코드 수정**: 아까 드린 스프링 부트 테스트 코드 작성
3. **변경 사항 확인**: `git status`
4. **스테이징**: `git add .`
5. **커밋**: `git commit -m "feat: 테스트용 API 컨트롤러 및 유닛 테스트 추가"`
6. **푸시**: `git push origin feat/test-api`
7. **메인 브랜치로 이동 (checkout 또는 switch)** `git checkout main`
8. **깃허브에 합쳐진 최신 코드 당겨오기 (동기화) -> ★가장 중요★** `git pull origin main`

### 5. 아까 작업하던 브랜치로 이동
`git checkout fix/infra-api-standardization`

### 6. 최신화된 메인 브랜치 내용을 내 작업 브랜치로 가져오기 (동기화) - 작업 하던 브랜치에서 재 진행
`git merge main`
