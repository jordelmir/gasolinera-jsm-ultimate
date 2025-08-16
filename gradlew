#!/usr/bin/env sh

#
# Copyright 2015 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# IMPORTANT: You may need to adapt the following lines to your specific project structure.
# You need to locate the gradle-wrapper.jar and the gradle-wrapper.properties file.
# If the gradlew script is not in the same directory as the gradle subdirectory, you need to
# point to the correct path to the gradle-wrapper.jar and the gradle-wrapper.properties file.
#
# For this script to be relocatable, we dynamically determine its location.
# You can override the location of the gradlew script by setting the GRADLEW_DIR environment variable.
#
# By default, we assume that the gradlew script is in the same directory as the gradle subdirectory.
# If you move the gradlew script, you may need to set GRADLE_USER_HOME, GRADLE_OPTS, etc.
#
APP_HOME="$(cd "${GRADLEW_DIR:-"$(dirname -- "$0")"}" && pwd -P)" || exit
# We need to set the an absolute path to the wrapper jar, so that the gradlew script can be
# invoked from any directory.
WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
WRAPPER_PROPERTIES="$APP_HOME/gradle/wrapper/gradle-wrapper.properties"

# You can override the java executable by setting the JAVA_CMD environment variable.
# You can override the java options by setting the JAVA_OPTS environment variable.
#
# The java options are passed to the java command. The JAVA_OPTS environment variable is
# not used by the gradlew script directly. The JAVA_OPTS are passed to the gradle client
# application, which in turn passes them to the gradle daemon.
# The JAVA_OPTS are not used to start the gradle daemon. The gradle daemon is started with the options that
# are defined in the gradle.properties file.
#
# You can pass gradle properties to the gradlew script. The gradle properties are passed
# to the gradle client application, which in turn passes them to the gradle daemon.
# The gradle properties are not used to start the gradle daemon. The gradle daemon is
# started with the options that are defined in the gradle.properties file.
#
# You can pass jvm options to the gradlew script. The jvm options are passed to the
# gradle client application, which in turn passes them to the gradle daemon.
# The jvm options are not used to start the gradle daemon. The gradle daemon is
# started with the options that are defined in the gradle.properties file.
#
# The following is a brief description of the options that can be passed to the gradlew script.
#
# -Dorg.gradle.appname=gradlew - is the name of the script that is used to start the gradle
#   client application. This is used to identify the gradle client application in the
#   process list.
# -classpath "$WRAPPER_JAR" - is the classpath that is used to start the gradle client
#   application.
# org.gradle.wrapper.GradleWrapperMain - is the main class that is used to start the gradle
#   client application.
# "$@" - is the list of arguments that are passed to the gradle client application.
#
# The gradlew script is a simple script that starts the gradle client application. The
# gradle client application is a simple java application that downloads the gradle
# distribution, and then starts the gradle daemon. The gradle daemon is a long-lived
# process that executes the builds. The gradle client application communicates with the
# gradle daemon using a socket. The gradle client application is responsible for
# starting the gradle daemon, and for passing the command-line arguments to the gradle
# daemon. The gradle daemon is responsible for executing the builds.
#
# The gradlew script is designed to be used in a CI/CD environment. It can be used to
# build the project without having to install gradle. The gradlew script will download
# the gradle distribution, and then start the gradle daemon. The gradle daemon will
# execute the builds.
#
# You can customize the gradlew script to suit your needs. You can change the location of
# the gradle-wrapper.jar and the gradle-wrapper.properties file. You can also change the
# java executable and the java options.
#
# For more information about the gradlew script, see the gradle documentation.
# https://docs.gradle.org/current/userguide/gradle_wrapper.html
#
# For more information about the gradle client application, see the gradle documentation.
# https://docs.gradle.org/current/userguide/command_line_interface.html
#
# For more information about the gradle daemon, see the gradle documentation.
# https://docs.gradle.org/current/userguide/gradle_daemon.html
#
exec "${JAVA_CMD:-java}" "" -Dorg.gradle.appname=gradlew -classpath "$WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
