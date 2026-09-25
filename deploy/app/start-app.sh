#!/bin/sh
set -eu

: "${DATABASE_URL:?DATABASE_URL is required}"

case "$DATABASE_URL" in
    jdbc:postgresql://*)
        export SPRING_DATASOURCE_URL="$DATABASE_URL"
        ;;
    postgresql://*)
        export SPRING_DATASOURCE_URL="jdbc:$DATABASE_URL"
        ;;
    *)
        echo "DATABASE_URL must use jdbc:postgresql:// or postgresql://" >&2
        exit 1
        ;;
esac

exec java "$@"
