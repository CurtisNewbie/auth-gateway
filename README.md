# auth-gateway v1.1.1

Simple gateway for my personal use.

To compile this app, you will also need to manually install the following modules & dependencies, these are all my repositories.

***Do not run the 'build' scripts, these are written for my development environment only***

## Requirements 

- Consul 
- RabbitMQ
- Redis
- goauth [goauth >= v1.0.0](https://github.com/CurtisNewbie/goauth/tree/v1.0.0)

## Modules and Dependencies

This project depends on the following modules that you must manually install (using `mvn clean install`).

- [curtisnewbie-bom](https://github.com/CurtisNewbie/curtisnewbie-bom)
- [common-module v2.2.0](https://github.com/CurtisNewbie/common-module/tree/v2.2.0)
- [jwt-module v1.0.1](https://github.com/CurtisNewbie/jwt-module/tree/v1.0.1)

## Update

- Starting from v1.1.1, `goauth` is required for path level authorization and resource management.

- Starting from v1.0.8, `auth-gateway` has changed from `Nacos` to `Consul`. Both the `file-service` and `auth-service` have changed to `Consul` at version `v1.2.4` and `v1.1.4.2` as well. If the `v1.0.8` version is used for auth-gateway, all the other services must be upgraded to appropriate versions, because `auth-gateway` relies on service discovery to route requests.


