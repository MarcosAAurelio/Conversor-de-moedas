-- Run as the original owner credential for the Render Postgres database.
-- The Render credentials converter_app and converter_migrator must already exist.
REVOKE CREATE ON SCHEMA public FROM PUBLIC;

GRANT CONNECT ON DATABASE currency_converter TO converter_migrator, converter_app;
GRANT USAGE, CREATE ON SCHEMA public TO converter_migrator;
GRANT USAGE ON SCHEMA public TO converter_app;
