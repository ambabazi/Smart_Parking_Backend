#!/usr/bin/env bash
# Convert a Render/Heroku style DATABASE_URL into JDBC and print DB env vars
# Usage: ./scripts/convert_database_url.sh 'postgres://user:pass@host:5432/dbname'

set -euo pipefail

if [ "$#" -ne 1 ]; then
  echo "Usage: $0 'postgres://user:pass@host:5432/dbname'" >&2
  exit 2
fi

RAW="$1"
# strip postgres:// or postgresql://
NO_PREFIX=$(echo "$RAW" | sed -E 's#^postgres(?:ql)?://##')

USERPASS=${NO_PREFIX%%@*}
HOST_PORT_DB=${NO_PREFIX#*@}

DB_USER=${USERPASS%%:*}
DB_PASS=${USERPASS#*:}
DB_HOST=${HOST_PORT_DB%%/*}
DB_NAME=${HOST_PORT_DB#*/}

DB_URL="jdbc:postgresql://${DB_HOST}/${DB_NAME}"

echo "DB_URL=${DB_URL}"
echo "DB_USERNAME=${DB_USER}"
echo "DB_PASSWORD=${DB_PASS}"

exit 0
