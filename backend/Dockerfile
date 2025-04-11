FROM eclipse-temurin:23-jdk-alpine
COPY ./build/libs/*SNAPSHOT.jar project.jar
ENTRYPOINT ["/bin/sh", "-c", "\
  echo 'Firebase Admin SDK 내용:' && \
  cat coing-firebase-adminsdk.json && \
  echo '프로젝트 실행 시작' && \
  java -jar project.jar \
"]
