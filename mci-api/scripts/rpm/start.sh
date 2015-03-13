#!/bin/sh
nohup java -Dserver.port=$MCI_PORT -jar $PROG_FILE > /var/log/mci/mci.log &
echo $! > /var/run/mci/mci.pid
