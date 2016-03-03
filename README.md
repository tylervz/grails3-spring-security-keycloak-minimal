# Getting Started

_This assumes you have Docker installed and the Docker host IP is 192.168.99.100. If you use a different IP, consider find/replace._

1. Run `./setup.sh` to run and provision a Keycloak server
2. Start each Grails application in a separate terminal using `grails run-app`
3. Browse to http://localhost:8080/client/ to access the client, which will dump a JSON returned by the service (KeycloakPrincipal)

## Versions

- CentOS 7
- OpenJDK 8
- Keycloak 1.9.0.Final (standalone)
- Grails 3.1.3 (with Gradle 2.9)

# How it works

TODO