#!/bin/bash

jarname="auth-gateway-build.jar"

mvn clean package -f auth-gateway/pom.xml -Dmaven.test.skip=true
if [ ! $? -eq 0 ]; then
    exit -1
fi

scp "auth-gateway/target/${jarname}" "alphaboi@curtisnewbie.com:~/services/auth-gateway/build/auth-gateway.jar"
if [ ! $? -eq 0 ]; then
    exit -1
fi

ssh  "alphaboi@curtisnewbie.com" "cd services; docker-compose up -d --build auth-gateway"
