#!/bin/sh
set -eu

: "${PORT:=8080}"
: "${APP_UPSTREAM:?APP_UPSTREAM must contain the private application host and port}"
DNS_RESOLVER="$(awk '$1 == "nameserver" { print $2; exit }' /etc/resolv.conf)"
: "${DNS_RESOLVER:?No DNS resolver is configured in /etc/resolv.conf}"
export PORT APP_UPSTREAM DNS_RESOLVER

mkdir -p /tmp/client_body /tmp/proxy_temp
envsubst '${PORT} ${APP_UPSTREAM} ${DNS_RESOLVER}' \
    < /etc/nginx/default.conf.template \
    > /tmp/nginx.conf
nginx -t -c /tmp/nginx.conf

if [ "${1:-}" = "--check" ]; then
    exit 0
fi

exec nginx -g 'daemon off;' -c /tmp/nginx.conf
