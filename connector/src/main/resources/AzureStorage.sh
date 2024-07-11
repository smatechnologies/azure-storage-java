#!/bin/bash
SCRIPT_DIR=$(dirname "$0")
cd $SCRIPT_DIR

java -cp msazure.storage.connector.jar com.smatechnologies.msazure.storage.connector.StorageConnector "$@"
