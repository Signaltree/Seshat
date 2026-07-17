#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
export DB_URL="${DB_URL:-jdbc:postgresql://localhost:5432/seshat}"
export DB_USER="${DB_USER:-signaltree}"
export DB_PASSWORD="${DB_PASSWORD:-}"
export ADMIN_USER="${ADMIN_USER:-admin}"
export ADMIN_PASS="${ADMIN_PASS:-admin123}"
mvn spring-boot:run "$@"
