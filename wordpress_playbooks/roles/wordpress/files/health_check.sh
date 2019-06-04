#!/bin/sh

URL=$1
PORT=$2

curl -Is http://$URL:$PORT/info.php > /dev/null && echo "The remote site is healthy" || echo "The remote site failed, please check..."