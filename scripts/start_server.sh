#!/bin/bash

set -e

JAR=$(ls -t /home/ec2-user/app2/*.jar | head -1)

cp -f "$JAR" /home/ec2-user/app2/app.jar

sudo systemctl daemon-reload
sudo systemctl restart springboot
sudo systemctl restart nginx