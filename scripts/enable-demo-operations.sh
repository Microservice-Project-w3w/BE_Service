#!/usr/bin/env bash
set -euo pipefail
project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
if [ ! -f "$project_root/.env" ]; then
  echo "Missing .env. Copy .env.example first." >&2
  exit 1
fi
mysql_password="$(awk -F= '$1 == "MYSQL_ROOT_PASSWORD" { sub("^[^=]*=", ""); value=$0 } END { print value }' "$project_root/.env")"
if [ -z "$mysql_password" ]; then
  echo "MYSQL_ROOT_PASSWORD is required." >&2
  exit 1
fi
docker compose --env-file "$project_root/.env" -f "$project_root/infra/docker-compose.yml" \
  exec -T -e "MYSQL_PWD=$mysql_password" mysql mysql --default-character-set=utf8mb4 -uroot \
  < "$project_root/infra/mysql/migrations/20261011_demo_operations.sql"
echo "Demo billing, handover, maintenance and sync tables are ready; existing data is unchanged."
