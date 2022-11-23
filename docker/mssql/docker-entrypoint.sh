#!/bin/bash
echo ">> Starting SQL Server"
docker-entrypoint-initdb.sh & /opt/mssql/bin/sqlservr
