FROM openjdk:17

# 빌드된 JAR 파일 경로 설정
ARG JAR_FILE=build/libs/Gunpo-0.0.1-SNAPSHOT.jar

# JAR 파일을 도커 이미지 내부로 복사
COPY ${JAR_FILE} app.jar

# 컨테이너 실행 시 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]