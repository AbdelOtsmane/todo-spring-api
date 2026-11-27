-- Executer avec psql en tant qu'administrateur, une seule fois.
CREATE ROLE todo LOGIN;
\password todo
CREATE DATABASE todo OWNER todo;
\connect todo
SET ROLE todo;
\ir schema.sql
