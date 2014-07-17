#!/bin/bash

test -f /etc/profile && source /etc/profile
test -f $HOME/.bash_profile &&source $HOME/.bash_profile

THIS=$(cd ${0%/*} && echo $PWD/${0##*/})
BASEDIR=`dirname ${THIS}`
BASEDIR=`dirname ${BASEDIR}`

APP_LOGCONF_DIR=${APP_LOGCONF_DIR:-${BASEDIR}/etc/swapmeat}

echo "Loading logging config from: ${APP_LOGCONF_DIR}"

CP="${APP_LOGCONF_DIR}:$BASEDIR/lib/swapmeat.jar"
for f in $(find $BASEDIR/lib/thirdparty -type f)
do
  CP=${CP}:${f}
done

JAVA=`which java`
$JAVA -version 2>&1 > /dev/null
if [ $? != 0 ]; then
  PATH=${JAVA_HOME}/bin:${PATH}
  JAVA=${JAVA_HOME}/bin/java
fi

APP_ENV=${APP_ENV:-${BASEDIR}/etc/swapmeat/env.sh}
test -f ${APP_ENV} && source ${APP_ENV}

JAVA_OPTS="$JAVA_OPTS $JAVA_DEBUG_OPTS"

exec "$JAVA" ${JAVA_OPTS} -cp "${CP}" -Dapp.home="${BASEDIR}" -Dapp.boot.defaults=${BASEDIR}/bin/boot.properties org.commonjava.swapmeat.cli.Booter "$@"

