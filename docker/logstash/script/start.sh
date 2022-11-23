#!/bin/bash

set -e

echo "---------------------------"
echo "LAUNCHING LOGSTASH INSTANCE"
echo "---------------------------"
echo "Environment setup is:"
echo "---------------------------"
java -version
groovy -version
echo "---------------------------"
echo "Setting up Logstash/Elastic:"
echo "---------------------------"
su -p -c "PATH=$PATH && groovy alromos/script/setupElasticLogstash.groovy" logstash
echo "---------------------------"
echo "Launching Logstash instance:"
echo "---------------------------"
su -p -c "PATH=$PATH && /usr/local/bin/docker-entrypoint" logstash
