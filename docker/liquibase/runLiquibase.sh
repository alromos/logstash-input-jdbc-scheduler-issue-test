#!/bin/bash
set -e

# Check what operation is to be run
echo ">> Updating Alromos schema..."
./liquibase --url="$DB_URL" \
            --changeLogFile="changelog/changelog.xml" \
            --username="$DBA_USER" \
            --password="$DBA_PASSWORD" \
            --defaultSchemaName="Alromos" \
            --logLevel=info \
            update

echo ">> Database update completed"
