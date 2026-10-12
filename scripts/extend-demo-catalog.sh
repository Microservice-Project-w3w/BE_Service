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
ready="$("${compose[@]}" exec -T -e "MYSQL_PWD=$mysql_password" mysql mysql --default-character-set=utf8mb4 -uroot -N -B -e \
  "SELECT COUNT(*) FROM organization_customer_db.organizations o
   JOIN organization_customer_db.branches b ON b.organization_id=o.id AND b.branch_code='DEMO-HN'
   JOIN inventory_db.warehouses w ON w.organization_id=o.id AND w.branch_id=b.id AND w.code='DEMO-HN-WH' AND w.active=TRUE
   WHERE o.organization_code='DEMO-RENTAI';")"
if [ "$ready" != "1" ]; then
  echo "Base demo organization/branch/active warehouse missing. Load the base demo on a fresh dev DB first." >&2
  exit 1
fi
"${compose[@]}" exec -T -e "MYSQL_PWD=$mysql_password" mysql mysql --default-character-set=utf8mb4 -uroot \
  < "$project_root/infra/mysql/seed/07-customer-catalog-demo.sql"
echo "Catalog extended; known demo encoding repaired. Existing asset/workflow states preserved. Refresh the customer catalog."
