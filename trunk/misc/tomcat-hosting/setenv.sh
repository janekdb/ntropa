# setenv.sh
# 
# This file is sourced by catalina.sh

# This defines the default location for the main configuration file
DEFAULT_NTROPA_CONF_FILE=/etc/ntropa/ntropa.properties
# Use "" if JAVA_OPTS is null or undefined.
JAVA_OPTS="${JAVA_OPTS:-""} -Dntropa.configuration-file=${DEFAULT_NTROPA_CONF_FILE}"
# If NTROPA_CONF_FILE is set then pass it on as the file to use.
if [ -n "${NTROPA_CONF_FILE}" ]; then
  JAVA_OPTS="${JAVA_OPTS} -Dntropa.configuration-file-alternative=${NTROPA_CONF_FILE}"
fi

# JRE_HOME and CATALINA_PID must be defined in the environment file.
#
# If ENV_FILE is unset or null assign a default value to it.
#
if [ -r ${NTROPA_ENV_FILE:=/etc/ntropa/environment.sh} ] ; then
  . ${NTROPA_ENV_FILE}
else
  echo "${NTROPA_ENV_FILE} was missing"
fi

# If environment.sh contained Java options apply them.

# Ubuntu 7.10 uses dash for sh. if [[ -n .. ]] does not work with dash.
if [ -n "${COMMON_JAVA_OPTS}" ] ; then
  JAVA_OPTS="${JAVA_OPTS} ${COMMON_JAVA_OPTS}"
fi

if [ "$1" = "start" ] && [ -n "${START_JAVA_OPTS}" ] ; then
  JAVA_OPTS="${JAVA_OPTS} ${START_JAVA_OPTS}"
fi

if [ "$1" = "stop" ] && [ -n "${STOP_JAVA_OPTS}" ] ; then
  JAVA_OPTS="${JAVA_OPTS} ${STOP_JAVA_OPTS}"
fi

