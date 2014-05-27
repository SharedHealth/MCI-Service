#!/bin/sh

ln -s /opt/mci/bin/mci /etc/init.d/mci
ln -s /opt/mci/etc/mci /etc/default/mci
ln -s /opt/mci/var /var/run/mci

if [ ! -e /var/log/mci ]; then
    mkdir /var/log/mci
fi

# Add mci service to chkconfig
chkconfig --add mci