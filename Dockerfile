# syntax=docker/dockerfile:1

# 1) 빌드: Gradle Wrapper로 실행 가능한 jar 생성 (테스트는 CI/로컬에서 실행)
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon -q > /dev/null
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# 2) 실행: JRE만 포함한 가벼운 이미지
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd --system --uid 1001 santadao
COPY --from=build /workspace/build/libs/*.jar app.jar
USER santadao
EXPOSE 8080
# Render 무료 플랜(512MB, 저사양 CPU) 기준
#  - MaxRAMPercentage: 컨테이너 메모리의 75%까지 힙 사용
#  - TieredStopAtLevel=1: JIT 최적화 단계를 줄여 기동 시 CPU 사용량 감소 (기동 속도 개선)
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC -XX:TieredStopAtLevel=1"
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
