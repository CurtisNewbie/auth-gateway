#!/bin/bash

jarname="auth-gateway-build.jar"

mvn clean package -f ../auth-gateway/pom.xml -Dmaven.test.skip=true

scp "../auth-gateway/target/${jarname}" "zhuangyongj@curtisnewbie.com:~/services/auth-gateway/build/auth-gateway.jar"
