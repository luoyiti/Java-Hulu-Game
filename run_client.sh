#!/usr/bin/env bash
set -e

cd "$(dirname "$0")"

mvn -q clean compile
mvn dependency:build-classpath -Dmdep.outputFile=cp.txt -q

echo "使用渲染后端: GPU (Client)"

CP="target/classes:$(cat cp.txt)"
java -XstartOnFirstThread -cp "$CP" com.gameengine.app.ClientGameLauncher