#!/bin/sh
nohup java -Dserver.port=$MCI_PORT -DMCI_LOG_LEVEL=$MCI_LOG_LEVEL -jar  /opt/mci/lib/mci-api.war > /dev/null 2>&1  &
echo $! > /var/run/mci/mci.pid
