#!/bin/sh
# Maven Wrapper script - downloads and runs Maven automatically
# Licensed to the Apache Software Foundation (ASF)

set -e

if [ -z "$JAVA_HOME" ]; then
  JAVACMD=java
else
  JAVACMD="$JAVA_HOME/bin/java"
fi

WRAPPER_JAR=".mvn/wrapper/maven-wrapper.jar"
WRAPPER_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar"

if [ ! -f "$WRAPPER_JAR" ]; then
  echo "Downloading Maven Wrapper..."
  if command -v curl > /dev/null 2>&1; then
    curl -sL -o "$WRAPPER_JAR" "$WRAPPER_URL"
  elif command -v wget > /dev/null 2>&1; then
    wget -q -O "$WRAPPER_JAR" "$WRAPPER_URL"
  fi
fi

MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-$(cd "$(dirname "$0")" && pwd)}"
DIST_URL=$(grep 'distributionUrl' "$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties" | cut -d'=' -f2-)
DIST_NAME=$(basename "$DIST_URL" .zip)
MAVEN_HOME="$HOME/.m2/wrapper/dists/$DIST_NAME"

if [ ! -d "$MAVEN_HOME" ]; then
  echo "Downloading Maven distribution..."
  mkdir -p "$MAVEN_HOME"
  if command -v curl > /dev/null 2>&1; then
    curl -sL "$DIST_URL" -o "/tmp/maven-dist.zip"
  elif command -v wget > /dev/null 2>&1; then
    wget -q "$DIST_URL" -O "/tmp/maven-dist.zip"
  fi
  unzip -q "/tmp/maven-dist.zip" -d "$MAVEN_HOME"
  rm -f "/tmp/maven-dist.zip"
fi

MVN_CMD=$(find "$MAVEN_HOME" -name "mvn" -path "*/bin/mvn" | head -1)
exec "$MVN_CMD" "$@"
