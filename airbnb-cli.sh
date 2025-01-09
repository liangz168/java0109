#!/bin/bash


if [ "$2" = "consumer" ]; then
  java -jar target/test-0.0.1.jar --my.runType=consumer --server.port=8083 "$3" "$4"
fi
if [ "$2" = "producer" ]; then
  java -jar target/test-0.0.1.jar --my.runType=producer --server.port=8082 "$3" "$4" "$5"
fi
