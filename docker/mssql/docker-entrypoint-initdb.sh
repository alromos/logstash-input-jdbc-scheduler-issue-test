#!/bin/bash

# wait for database to start...
echo ">> SQL Server startup in progress"
until sqlcmd -U SA -P "$SA_PASSWORD" -Q 'SELECT 1;' &> /dev/null; do
  echo -n "."
  sleep 1
done

echo ">> Initializing database"
DIR="/docker-entrypoint-initdb.d"
db=$(sqlcmd -U SA -P "$SA_PASSWORD" -Q "sp_databases" -v DBA_USER=$DBA_USER \
                                                         DBA_PASSWORD=$DBA_PASSWORD \
                                                         APP_USER=$APP_USER \
                                                         APP_PASSWORD=$APP_PASSWORD | grep "$DB_NAME")
if [[ $db =~ $DB_NAME ]]; then
  echo ">> Database already initialized"
  exit
else
  echo ">> executing as SA: $DIR/$SA_INIT_SCRIPT"
  sqlcmd -U SA -P "$SA_PASSWORD" -X -i "$DIR/$SA_INIT_SCRIPT"
fi

PREFIX="[0-9]{2}-"
for f in "$DIR"/*; do
  if [[ "$f" =~ $PREFIX(.*).sql ]]; then
    echo ">> executing as $DBA_USER: $f"
    sqlcmd -U "$DBA_USER" -P "$DBA_PASSWORD" -d "$DB_NAME" -X -i "$f"
  elif [[ "$f" =~ $PREFIX(.*).sh ]]; then
    echo ">> executing $f"
    . "$f"
  fi
done
echo ">> SQL Server Database ready"
