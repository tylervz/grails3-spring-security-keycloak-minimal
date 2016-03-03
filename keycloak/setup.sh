#!/bin/bash

KEYCLOAK_URI=http://localhost:8080/auth
REALM_JSON=/opt/keycloak/grails-realm.json
JBOSS_CLI=/opt/keycloak/keycloak-1.9.0.Final/bin/jboss-cli.sh

echo -e "Starting Keycloak in background..."
/opt/keycloak/keycloak-1.9.0.Final/bin/standalone.sh -b 0.0.0.0 -bmanagement 0.0.0.0 &
until `$JBOSS_CLI -c "ls /deployment" &> /dev/null`; do
    sleep 1
done

ACCESS_TOKEN=$(curl -s -X POST $KEYCLOAK_URI/realms/master/protocol/openid-connect/token \
        -d grant_type=password \
        -d client_id=admin-cli \
        -d username=admin -d password=admin | jq -r '.access_token')

# Remove existing realm
curl -s -S -X DELETE -H "Authorization: Bearer $ACCESS_TOKEN" $KEYCLOAK_URI/admin/realms/grails

echo "Importing realm..."
curl -s -S -H "Content-Type: application/json" -H "Authorization: Bearer $ACCESS_TOKEN" -d @$REALM_JSON $KEYCLOAK_URI/admin/realms

echo "Shutting down..."
$JBOSS_CLI -c ':shutdown' &> /dev/null
