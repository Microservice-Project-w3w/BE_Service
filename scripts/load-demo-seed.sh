#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
seed_directory="$project_root/infra/mysql/seed"

if [ ! -f "$project_root/.env" ]; then
  echo "Missing $project_root/.env. Copy .env.example first." >&2
  exit 1
fi

read_env_value() {
  awk -F= -v key="$1" '$1 == key { sub("^[^=]*=", ""); value = $0 } END { print value }' "$project_root/.env"
}

mysql_password="$(read_env_value MYSQL_ROOT_PASSWORD)"
if [ -z "$mysql_password" ]; then
  echo "MYSQL_ROOT_PASSWORD is required in .env." >&2
  exit 1
fi

compose=(docker compose --env-file "$project_root/.env" -f "$project_root/infra/docker-compose.yml")
role_count="$("${compose[@]}" exec -T -e "MYSQL_PWD=$mysql_password" mysql mysql -uroot -N -B -e "SELECT COUNT(*) FROM identity_db.roles" 2>/dev/null || true)"
if [ "${role_count:-0}" -eq 0 ]; then
  echo "Identity role seed is missing. Start identity-service once, then rerun this command." >&2
  exit 1
fi

bash "$project_root/scripts/enable-customer-portal-demo.sh"

for seed_file in \
  "$seed_directory/01-organization-customer-demo.sql" \
  "$seed_directory/02-identity-demo.sql" \
  "$seed_directory/03-organization-customer-assignments-demo.sql" \
  "$seed_directory/04-inventory-demo.sql" \
  "$seed_directory/05-rental-demo.sql" \
  "$seed_directory/06-customer-portal-demo.sql"; do
  echo "Loading $(basename "$seed_file")"
  "${compose[@]}" exec -T -e "MYSQL_PWD=$mysql_password" mysql mysql --default-character-set=utf8mb4 -uroot < "$seed_file"
done

echo "Demo data loaded. Demo account password: Demo@123"
