#FROM openjdk:8-jdk-alpine
FROM openjdk:17-alpine

LABEL author="yongjie.zhuang"
LABEL descrption="Auth Gateway"

COPY . /usr/src/auth-gateway
WORKDIR /usr/src/auth-gateway

ENV TZ=Asia/Shanghai

EXPOSE 7070/tcp
EXPOSE 7070/tcp

CMD ["java", "-jar", "auth-gateway.jar", "--spring.profiles.active=test"]
