#!/bin/bash
#
# Temporary install script

set -u

echo TODO TEST useradd --system UNDER DEBIAN
exit

if [ $(id -u) != 0 ] ; then
    printf "Must be run as root.\nExiting...\n"
    exit 1
fi

declare -r APP_DIR=build/tomcat-hosted-ntropa-builder/apache-tomcat-5.5.20

if [ ! -d ${APP_DIR} ] ;then
  echo "${APP_DIR} was missing"
  exit 1
fi

APP_USER=ntropa

# Edgy uses dash for sh and this does not work in dash: declare -r INSTALL_DIR=/var/lib/ntropa
declare -r INSTALL_DIR=/var/lib/ntropa
rm -rf ${INSTALL_DIR}

cp -r ${APP_DIR} ${INSTALL_DIR}
declare -r APP_HOME=${INSTALL_DIR}/home
# useradd will only be run once per machine so always add the home directory.
mkdir ${APP_HOME}
# Show error
if ! id ${APP_USER} >/dev/null ; then
  echo "The user ${APP_USER} did not exist. Creating it now."
  useradd --system --home ${APP_HOME} --shell /bin/bash ${APP_USER}
fi

# Give ntropa directory write permissions by changing the owner to ntropa

# conf/    because tomcat-users.xml is written out on shutdown
# home/    so jvm crash dumps can be written
# logs/    for logs
# tmp/     for temporary files
# webapps/ because builder.jar is exploded to ROOT/
# work/    because the compiled JSPs are stored here.
cd ${INSTALL_DIR}
chown -R ${APP_USER} conf home logs temp webapps work

# Add a demo setup and configure ntropa to work on this.
(
  cd ${INSTALL_DIR}
  mkdir -p demo/{input,link,output}/{master,site-a,site-b}
  chown ${APP_USER} demo/*/*
)

declare -r CONF_DIR=/etc/ntropa
declare -r PF=${CONF_DIR}/ntropa.properties

if [ ! -d ${CONF_DIR} ] ; then
  mkdir -m 755 ${CONF_DIR}
  declare -r PF_DEMO=${PF}-demo
  cat << EOF > ${PF_DEMO}
# This is a demonstation configuration.
# Replace this with your own configuration.
#
context-path-list=master,site-a,site-b
configuration.version=1.0
layout.input=${INSTALL_DIR}/demo/input
layout.link=${INSTALL_DIR}/demo/link
layout.output=${INSTALL_DIR}/demo/output
scheduler.period=10
EOF
  cp ${PF_DEMO} ${PF}
  chmod 644 ${PF_DEMO} ${PF}

  # Add the environment file
  declare -r ENV_FILE=${CONF_DIR}/environment.sh

  # REQUEST FOR HELP
  # If you have a better way of determining JRE_HOME for your distro please
  # tell us. This has been tested on Kubuntu Edgy and Fiesty.
  JRE_HOME_GUESS=""
  if JAVA=$(which java); then
    #  -e, --canonicalize-existing
    #    canonicalize  by  following  every symlink in every component of
    #    the given name recursively, all components must exist
    if JAVA=$(readlink -e ${JAVA}); then
      # /usr/lib/jvm/java-1.5.0-sun/bin/java -> /usr/lib/jvm/java-1.5.0-sun
      # /usr/lib/jvm/java-6-sun-1.6.0.00/jre/bin/java -> /usr/lib/jvm/java-6-sun-1.6.0.00/jre
      JRE_HOME_GUESS=$(dirname $(dirname ${JAVA}))
      echo "JRE_HOME_GUESS: ${JRE_HOME_GUESS}"
    fi
  fi

  cat << EOF > ${ENV_FILE}
# CATALINA_PID is the file which ntropa will put it's pid into.
# This variable is used in catalina.sh.
# There is no usually any need to change this because it was set
# correctly by the installer.
#
CATALINA_PID=${APP_HOME}/ntropa.pid


# JRE_HOME should be the location to Java Runtime Environment used to
# run ntropa with. The installer inserts it's best guess here.
#
# Example values are:
# 
#   /usr/lib/jvm/jdk1.6.0/jre
#   /usr/lib/jvm/java-6-sun-1.6.0.03/jre
#   /opt/java-1.5/jre
#
# You are encouraged to change this to the path of your preferred JRE. Java 5 or later
# is required.
#
# Please read the note in the installer script regarding this "REQUEST FOR HELP".
#
JRE_HOME=${JRE_HOME_GUESS}
EOF
  chmod 644 ${ENV_FILE}

fi

