#!/bin/bash
JBOSS_CLI=/opt/keycloak/keycloak-1.9.0.Final/bin/jboss-cli.sh
REALM_JSON=/opt/keycloak/grails-realm.json

/opt/keycloak/keycloak-1.9.0.Final/bin/standalone.sh -b 0.0.0.0 -bmanagement 0.0.0.0 
