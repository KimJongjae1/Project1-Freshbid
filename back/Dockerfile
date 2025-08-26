# --- 1. 빌드(Build) 스테이지 ---
# Gradle과 JDK가 포함된 이미지를 빌드용으로 사용하고 'builder'라는 별칭을 부여합니다.
FROM gradle:8.5-jdk17-alpine AS builder

# 작업 디렉토리 설정
WORKDIR /build

# 빌드에 필요한 gradle 설정 파일을 먼저 복사하여 Docker 레이어 캐시를 활용합니다.
COPY build.gradle settings.gradle ./

# 의존성을 먼저 다운로드합니다. (소스코드가 변경되어도 의존성은 재사용 가능)
RUN gradle dependencies

# 전체 소스코드를 복사합니다.
COPY src ./src

# Gradle을 사용하여 애플리케이션을 빌드합니다. (테스트는 생략)
RUN gradle bootJar --no-daemon


# --- 2. 최종(Final) 스테이지 ---
# 실제 실행을 위한 가벼운 JRE 이미지를 사용합니다.
FROM openjdk:17-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app

# 빌드 스테이지('builder')에서 생성된 JAR 파일을 복사해옵니다.
COPY --from=builder /build/build/libs/*.jar app.jar
COPY .env ./

# 애플리케이션 포트 노출
EXPOSE 8088

# 컨테이너 시작 시 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
