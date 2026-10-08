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
compose=(docker compose --env-file "$project_root/.env" -f "$project_root/infra/docker-compose.yml")
for sql_file in "$project_root/infra/mysql/init/02-customer-portal.sql" "$project_root/infra/mysql/seed/06-customer-portal-demo.sql"; do
  "${compose[@]}" exec -T -e "MYSQL_PWD=$mysql_password" mysql mysql --default-character-set=utf8mb4 -uroot < "$sql_file"
done
echo "Customer portal schema/link ready. Log out and log in to refresh branch scope."
