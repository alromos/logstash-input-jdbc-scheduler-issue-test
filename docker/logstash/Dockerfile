FROM groovy:4.0.6-jdk17 AS alromos-groovy

FROM docker.elastic.co/logstash/logstash:8.5.1

USER root

# Groovy
COPY --from=alromos-groovy /opt/groovy /usr/share/logstash/groovy

# MSSQL Driver
RUN curl -L --output "/opt/logstash/logstash-core/lib/jars/mssql.jar" "https://repo1.maven.org/maven2/com/microsoft/sqlserver/mssql-jdbc/11.2.1.jre17/mssql-jdbc-11.2.1.jre17.jar"

# Logstash Configuration
COPY ./ /usr/share/logstash/alromos/
RUN \
  mkdir -p /usr/share/logstash/template && \
  mkdir -p /usr/share/logstash/last_run && \
  chown -R logstash:logstash /usr/share/logstash/alromos && \
  chown -R logstash:logstash /usr/share/logstash/template && \
  chown -R logstash:logstash /usr/share/logstash/last_run

ENV PATH /usr/share/logstash/jdk/bin/:$PATH
ENV GROOVY_HOME /usr/share/logstash/groovy
ENV PATH $GROOVY_HOME/bin/:$PATH

ENTRYPOINT ["/usr/share/logstash/alromos/script/start.sh"]
