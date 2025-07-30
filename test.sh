#!/usr/bin/env bash
set -euo pipefail

VERSION="0.1.0-SNAPSHOT"

# Run SBT tasks
sbt "assembly"
sbt "graalvm-native-image:packageBin"

# Verify that artifacts exist
JAR_PATH="target/scala-2.13/codacy-gosec-assembly-${VERSION}.jar"
NATIVE_PATH="target/graalvm-native-image/codacy-gosec"

if [[ ! -f "$JAR_PATH" ]]; then
  echo "❌ JAR file not found: $JAR_PATH"
  exit 1
fi

if [[ ! -f "$NATIVE_PATH" ]]; then
  echo "❌ Native image not found: $NATIVE_PATH"
  exit 1
fi

# Move artifacts to output folder
mkdir -p ~/workdir/artifacts/

cp "$JAR_PATH" ~/workdir/artifacts/codacy-gosec-${VERSION}.jar
cp "$NATIVE_PATH" ~/workdir/artifacts/codacy-gosec-${VERSION}

echo "✅ All artifacts created and copied successfully."
