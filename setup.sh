#!/bin/bash

docker rm $(docker stop keycloak)
docker build -t keycloak keycloak
docker run -it --name keycloak -p 8080:8080 keycloak
