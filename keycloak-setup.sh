#!/bin/bash

docker rm $(docker stop keycloak)
docker rm $(docker stop keycloak6)
docker build -t keycloak6 keycloak
docker run -it --name keycloak6 -p 8080:8080 keycloak6
