# auth-gateway v1.0.10

Simple gateway for my personal use.

To compile this app, you will also need to manually install the following modules & dependencies, these are all my repositories.

***Do not run the 'build' scripts, these are written for my development environment only***

## Requirements 

- Consul 
- RabbitMQ
- Redis

## Modules and Dependencies

This project depends on the following modules that you must manually install (using `mvn clean install`).

- [curtisnewbie-bom](https://github.com/CurtisNewbie/curtisnewbie-bom)
- [messaging-module v2.0.7](https://github.com/CurtisNewbie/messaging-module/tree/v2.0.7)
- [auth-service-remote v1.1.4.2](https://github.com/curtisnewbie/auth-service/tree/v1.1.4.2)
- [common-module v2.1.5](https://github.com/CurtisNewbie/common-module/tree/v2.1.5)
- [jwt-module v1.0.1](https://github.com/CurtisNewbie/jwt-module/tree/v1.0.1)

## Update

Starting from v1.0.8, `auth-gateway` has changed from `Nacos` to `Consul`. Both the `file-service` and `auth-service` have changed to `Consul` at version `v1.2.4` and `v1.1.4.2` as well. If the `v1.0.8` version is used for auth-gateway, all the other services must be upgraded to appropriate versions, because `auth-gateway` relies on service discovery to route requests.
