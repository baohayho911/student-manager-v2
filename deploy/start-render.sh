#!/bin/sh
set -eu
if [ ! -r /etc/secrets/mysql-ca.pem ]; then
  echo 'Missing /etc/secrets/mysql-ca.pem. Add the Aiven CA certificate as a Render Secret File named mysql-ca.pem.' >&2
  exit 1
fi
umask 077
rm -f /tmp/mysql-truststore.p12
keytool -importcert -noprompt -alias mysql-ca \
  -file /etc/secrets/mysql-ca.pem -keystore /tmp/mysql-truststore.p12 \
  -storetype PKCS12 -storepass changeit >/dev/null
exec java -Xmx256m -XX:+ExitOnOutOfMemoryError -jar /app/app.jar
