#!/bin/bash

DEBUG_DIRECTORY="debug/waterfall"
WATERFALL_DOWNLOAD="https://papermc.io/api/v1/waterfall/1.15/latest/download"
ROOT_DIR="$(pwd)"

mkdir -p $DEBUG_DIRECTORY
cd $DEBUG_DIRECTORY || exit

[ ! -f waterfall.jar ] && wget $WATERFALL_DOWNLOAD -O waterfall.jar

mkdir plugins
ln -sr  "$ROOT_DIR/target/NetworkUtilities-1.0.0.jar" plugins

java -Xmx2G -Xms2G -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005 -jar ./waterfall.jar
