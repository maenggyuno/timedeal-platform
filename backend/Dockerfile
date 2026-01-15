# Dockerfile (Backend - Spring Boot / Java 21)
# 
# ------------------------------------------------
# 1. 빌드(Build) 환경
# ------------------------------------------------
FROM openjdk:21-jdk-slim AS builder
WORKDIR /app

# Gradle 관련 파일만 먼저 복사
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle ./
COPY settings.gradle ./

# gradlew 스크립트에 실행 권한 부여
RUN chmod +x ./gradlew

# 의존성만 먼저 다운로드 (캐시됨)
RUN ./gradlew dependencies

# [✅ 수정된 부분]
# 모든 파일을 덮어쓰는(COPY . .) 대신,
# 소스 코드(src) 폴더만 복사합니다.
COPY src ./src

# [최종 빌드]
# 캐시된 의존성과 복사된 소스 코드를 사용하여 빌드합니다.
RUN ./gradlew build -x test

# ------------------------------------------------
# 2. 실행(Runtime) 환경
# ------------------------------------------------
FROM openjdk:21-slim
WORKDIR /app

# 빌드 환경에서 생성된 .jar 파일만 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 환경 변수(.env)를 Java 시스템 프로퍼티(-D)로 주입받아 앱을 실행
ENTRYPOINT ["java", \
            "-Dfrontend.url=${FRONTEND_URL}", \
            "-Dcookie.secure=${COOKIE_SECURE}", \
            "-Dspring.datasource.url=${DB_URL}", \
            "-Dspring.datasource.username=${DB_USERNAME}", \
            "-Dspring.datasource.password=${DB_PASSWORD}", \
            "-jar", "app.jar"]
