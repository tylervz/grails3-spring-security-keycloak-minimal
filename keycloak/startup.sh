#!/bin/bash
JBOSS_CLI=/opt/keycloak/keycloak-1.9.0.Final/bin/jboss-cli.sh
REALM_JSON=/opt/keycloak/grails-realm.json

/opt/keycloak/keycloak-1.9.0.Final/bin/standalone.sh -b 0.0.0.0 -bmanagement 0.0.0.0 

# The script continues below as soon as Wildfly is shut down

echo -e "Exporting realm to ${REALM_JSON}..."

/opt/keycloak/keycloak-1.9.0.Final/bin/standalone.sh -b 0.0.0.0 -bmanagement 0.0.0.0 \
    -Dkeycloak.migration.action=export \
    -Dkeycloak.migration.provider=singleFile \
    -Dkeycloak.migration.realmName=grails \
    -Dkeycloak.migration.file=$REALM_JSON &

while [ ! -f $REALM_JSON ]; do
    sleep 1
    echo -n .
done

echo

echo -e "Shutting down..."
$JBOSS_CLI -c ':shutdown' &> /dev/null
while `$JBOSS_CLI -c "ls /deployment" &> /dev/null`; do
    echo -n .
    sleep 1
done

echo