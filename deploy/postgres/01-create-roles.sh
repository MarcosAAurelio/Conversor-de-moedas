#!/bin/sh
set -eu

: "${DATABASE_APP_PASSWORD:?DATABASE_APP_PASSWORD is required}"
: "${DATABASE_MIGRATION_PASSWORD:?DATABASE_MIGRATION_PASSWORD is required}"
: "${POSTGRES_PASSWORD:?POSTGRES_PASSWORD is required}"

for password in "$POSTGRES_PASSWORD" "$DATABASE_APP_PASSWORD" "$DATABASE_MIGRATION_PASSWORD"; do
    case "$password" in
        replace-with-*) echo "Replace example passwords with unique random secrets before starting." >&2; exit 1 ;;
    esac
    if [ "${#password}" -lt 32 ]; then
        echo "Database passwords must contain at least 32 characters." >&2
        exit 1
    fi
done

if [ "$POSTGRES_PASSWORD" = "$DATABASE_APP_PASSWORD" ] \
    || [ "$POSTGRES_PASSWORD" = "$DATABASE_MIGRATION_PASSWORD" ] \
    || [ "$DATABASE_APP_PASSWORD" = "$DATABASE_MIGRATION_PASSWORD" ]; then
    echo "Use a different random password for each database role." >&2
    exit 1
fi

psql --set=ON_ERROR_STOP=1 \
    --username "$POSTGRES_USER" \
    --dbname "$POSTGRES_DB" \
    --set=app_password="$DATABASE_APP_PASSWORD" \
    --set=migration_password="$DATABASE_MIGRATION_PASSWORD" <<'SQL'
REVOKE CREATE ON SCHEMA public FROM PUBLIC;

CREATE ROLE converter_migrator LOGIN PASSWORD :'migration_password';
CREATE ROLE converter_app LOGIN PASSWORD :'app_password';

GRANT CONNECT ON DATABASE currency_converter TO converter_migrator, converter_app;
GRANT USAGE, CREATE ON SCHEMA public TO converter_migrator;
GRANT USAGE ON SCHEMA public TO converter_app;

ALTER DEFAULT PRIVILEGES FOR ROLE converter_migrator IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO converter_app;
ALTER DEFAULT PRIVILEGES FOR ROLE converter_migrator IN SCHEMA public
    GRANT USAGE, SELECT, UPDATE ON SEQUENCES TO converter_app;
SQL
