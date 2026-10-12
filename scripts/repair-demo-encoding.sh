#!/usr/bin/env bash
set -euo pipefail

case "${1:---check}" in
  --check) repair_apply=0 ;;
  --apply) repair_apply=1 ;;
  *) echo "Usage: bash scripts/repair-demo-encoding.sh [--check|--apply]" >&2; exit 2 ;;
esac

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
{
  printf 'SET @demo_encoding_apply = %s;\n' "$repair_apply"
  cat "$project_root/infra/mysql/seed/08-repair-demo-encoding.sql"
} | "${compose[@]}" exec -T -e "MYSQL_PWD=$mysql_password" mysql mysql --default-character-set=utf8mb4 -uroot
