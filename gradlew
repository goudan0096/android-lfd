#!/bin/sh

#
# Gradle start up script for POSIX
#

APP_HOME=$( cd "$(dirname "$0")" > /dev/null && pwd -P )
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

# Determine the Java command to use
if [ -n "$JAVA_HOME" ] ; then
    JAVACMD=$JAVA_HOME/bin/java
else
    JAVACMD=java
fi

# Set default JVM options
DEFAULT_JVM_OPTS='"-Dorg.gradle.appname=gradlew"'

# Use the maximum available file descriptors
MAX_FD="maximum"

# OS specific support
darwin=false
case "$( uname )" in
  Darwin* ) darwin=true ;;
esac

# Increase the maximum file descriptors if we can
if [ "$darwin" = "false" ] ; then
    case $MAX_FD in
      max*)
        MAX_FD=$( ulimit -H -n ) || echo "Could not query maximum file descriptor limit"
        ;;
    esac
    case $MAX_FD in
      '' | soft) :;;
      *)
        ulimit -n "$MAX_FD" || echo "Could not set maximum file descriptor limit to $MAX_FD"
        ;;
    esac
fi

# Collect all arguments for the java command
exec "$JAVACMD" \
    -Dorg.gradle.appname=gradlew \
    -classpath "$CLASSPATH" \
    org.gradle.wrapper.GradleWrapperMain "$@"
