#!/bin/sh

URL=$1

curl -Is http://$URL > /dev/null && echo "The remote site is healthy" || echo "The remote site failed, please check..."