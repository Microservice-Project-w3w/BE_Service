#!/usr/bin/env bash
set -euo pipefail

if [ "$#" -ne 1 ]; then
  echo "Usage: $0 {identity-service|organization-customer-service|inventory-service|rental-service|api-gateway}" >&2
  exit 1
fi

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
service_name="$1"

case "$service_name" in
  api-gateway)
    module="api-gateway"
    ;;
  identity-service|organization-customer-service|inventory-service|rental-service)
    module="services/$service_name"
    ;;
  *)
    echo "Unknown service: $service_name" >&2
    exit 1
    ;;
esac

cd "$project_root"

while IFS= read -r env_line || [ -n "$env_line" ]; do
  case "$env_line" in
    ''|'#'*) continue ;;
  esac

  env_key="${env_line%%=*}"
  env_value="${env_line#*=}"
  if [[ ! "$env_key" =~ ^[A-Za-z_][A-Za-z0-9_]*$ ]]; then
    echo "Invalid environment variable name in .env: $env_key" >&2
    exit 1
  fi
  export "$env_key=$env_value"
done < .env

if [ "${AI_SERVICE_RUNS_ON_WINDOWS:-false}" = "true" ]; then
  windows_host="${WSL_WINDOWS_HOST:-$(ip route show default | awk '/default/ { print $3; exit }')}"
  if [ -z "$windows_host" ]; then
    echo "Cannot resolve the Windows host IP from the WSL default route" >&2
    exit 1
  fi
  export AI_SERVICE_URL="http://${windows_host}:${AI_SERVICE_PORT:-8090}"
fi

echo "Starting $module (AI_SERVICE_URL=${AI_SERVICE_URL})"
echo "Installing shared modules required by the selected service..."
mvn -pl shared/common-web,shared/common-security,shared/event-contracts -am install -DskipTests
exec mvn -pl "$module" spring-boot:run
