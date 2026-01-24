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
