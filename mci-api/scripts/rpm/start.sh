#!/bin/sh
nohup java -Dserver.port=$MCI_PORT -jar /opt/mci/lib/mci-api.war > /var/log/mci/mci.log &
echo $! > /var/run/mci/mci.pid
