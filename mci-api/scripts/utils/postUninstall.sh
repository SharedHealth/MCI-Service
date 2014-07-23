#!/bin/sh

rm -f /etc/init.d/mci
rm -f /etc/default/mci
rm -f /var/run/mci

#Remove mci from chkconfig
chkconfig --del mci || true