FROM postgres:9.4.5
RUN apt-get update && apt-get install -y --force-yes pgagent
COPY postgres.conf /etc/postgresql/postgresql.conf
COPY entrypoint.sh docker-entrypoint.sh

