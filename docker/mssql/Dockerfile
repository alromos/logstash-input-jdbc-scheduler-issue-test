FROM mcr.microsoft.com/mssql/server:2019-CU18-ubuntu-20.04

VOLUME /docker-entrypoint-initdb.d
EXPOSE 1433

ENV PATH $PATH:/opt/mssql-tools/bin

COPY docker-entrypoint.sh /usr/local/bin/
COPY docker-entrypoint-initdb.sh /usr/local/bin/

ENTRYPOINT ["docker-entrypoint.sh"]
