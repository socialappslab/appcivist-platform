#!/bin/bash
cd /home/appcivist/production/appcivist-platform/conf/
source /environment
cp local.sample.conf local.conf
cp play-authenticate/mine.sample.conf play-authenticate/mine.local.conf
cp play-authenticate/smtp.sample.conf play-authenticate/smtp.local.conf
cd /home/appcivist/production/appcivist-platform/


./activator stage && /etc/init.d/appcivist-backend start