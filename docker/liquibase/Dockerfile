FROM liquibase/liquibase:4.17.2

RUN wget -O /liquibase/lib/mssql.jar https://repo1.maven.org/maven2/com/microsoft/sqlserver/mssql-jdbc/11.2.1.jre11/mssql-jdbc-11.2.1.jre11.jar && \
    wget -O /liquibase/lib/mssql.jar.asc https://repo1.maven.org/maven2/com/microsoft/sqlserver/mssql-jdbc/11.2.1.jre11/mssql-jdbc-11.2.1.jre11.jar.asc

COPY ./runLiquibase.sh runLiquibase.sh
COPY ./changelog changelog

ENTRYPOINT ["./runLiquibase.sh"]
