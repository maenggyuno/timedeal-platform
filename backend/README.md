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
-- 생성된 이름이 'order_items'(Snake Case)인지 'orderItems'(Camel Case)인지 반드시 체크!
```

---

### 2. 네이밍 컨벤션 (Naming Convention) - ⭐ 가장 중요!
윈도우(Local)와 리눅스(RDS/EC2)의 파일 시스템 차이로 인해 **대소문자 구분 이슈**가 발생합니다. 아래 표준을 반드시 따르세요.

* **Java (Code):** CamelCase (`OrderItems`)
* **Database (SQL):** SnakeCase (`order_items`)

**[해결 방법]**

1.  **Entity 클래스:** `@Table` 어노테이션으로 명시적 매핑
    ```java
    @Entity
    @Table(name = "order_items") // DB에는 무조건 이렇게 만들어라!
    public class OrderItems { ... }
    ```

2.  **Native Query (`@Query`):** 실제 DB 테이블 이름 사용
    ```java
    // (X) SELECT * FROM orderItems ... (리눅스에서 에러 남)
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

# 추천 설정: CamelCase -> snake_case 자동 변환
spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy

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
./gradlew build -x test
```

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
