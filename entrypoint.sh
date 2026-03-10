#!/bin/bash
set -e

# Extract database host from SPRING_DATASOURCE_URL (e.g., jdbc:mysql://ac-database:3306/acore_auth)
DB_HOST=$(echo "$SPRING_DATASOURCE_URL" | sed -n 's|.*://\([^:/]*\).*|\1|p')
DB_HOST=${DB_HOST:-ac-database}

echo "Waiting for database at $DB_HOST..."
echo "Using DB_ROOT_PASSWORD: ${DB_ROOT_PASSWORD:+[SET]}"

# Check if we can resolve the hostname first
echo "Checking DNS resolution for $DB_HOST..."
if ! getent hosts "$DB_HOST" > /dev/null 2>&1; then
  echo "WARNING: Cannot resolve hostname '$DB_HOST'. Make sure the database container is running and connected to the same network."
fi

MAX_RETRIES=30
RETRY_COUNT=0

until timeout 5 mysql -h "$DB_HOST" -u root -p"$DB_ROOT_PASSWORD" -e "SELECT 1" 2>&1; do
  RETRY_COUNT=$((RETRY_COUNT + 1))
  if [ $RETRY_COUNT -ge $MAX_RETRIES ]; then
    echo "ERROR: Could not connect to database after $MAX_RETRIES attempts. Exiting."
    exit 1
  fi
  echo "Waiting for database... (attempt $RETRY_COUNT/$MAX_RETRIES)"
  sleep 2
done

echo "Initializing database..."
export SPRING_HOST='%'
export SPRING_PASSWORD="$SPRING_DATASOURCE_PASSWORD"
envsubst '${SPRING_HOST} ${SPRING_PASSWORD}' < /app/init.sql.template \
  | mysql -h "$DB_HOST" -u root -p"$DB_ROOT_PASSWORD" -f

echo "Starting API..."
exec java -jar /app/app.jar