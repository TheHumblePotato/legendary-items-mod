#!/usr/bin/env sh

# Gradle startup script for UN*X

appname=$(basename "$0")
appdir=$(dirname "$0")
exe="$appdir/gradlew"

if [ -z "$JAVA_HOME" ] ; then
  JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
  export JAVA_HOME
fi

exec "$JAVA_HOME/bin/java" -Xmx1024m -Dorg.gradle.appname="$appname" -classpath "$appdir/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"