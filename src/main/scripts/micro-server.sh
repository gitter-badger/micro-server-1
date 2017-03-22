#!/usr/bin/env bash
umask 002
debug=0
#########################################################################
# MXDeploy
# Copyright MXDeploy   2016
# All Rights Reserved.
# Author:    Fabio Santos B. da Silva
# Version:   0.0.1
# Date:      07/05/2016
# Purpose:   Deployment Suite and Automation
# URL :      www.mxdeploy.com
#########################################################################

debug(){
    if [ $debug != 0 ]; then
        echo "DEBUG: $1"
    fi
}

error(){
    echo ""
    echo "Error: $1"
    echo ""
}

validateYml(){
    YML=$1
    if [ ! -f  "$YML" ]; then
       echo "Error: The file $YML doesn't exist"
       exit
    fi
}

if [ $# -eq 0 ]; then
   echo "Error : Argument doesn't exist. "
   echo "   Examples : mxd --start  "
   echo "              mxd --stop "
   echo "              mxd --restart "
   echo "              mxd --status "
   echo "              mxd --sessionExec <action-name> "
   exit
fi


PRG="$0"

# need this for relative symlinks
while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG="`dirname "$PRG"`/$link"
    fi
done

#MX_PARENT=`echo $PRG | sed 's|/mxd||g'`
MX_PARENT="/opt/mxdeploy"
MX_HOME="$MX_PARENT/micro-server"

debug "PRG: $PRG"
debug "MX_PARENT: $MX_PARENT"
debug "MX_HOME: $MX_HOME"

validateYml "$MX_PARENT/variables.yml"
validateYml "$MX_HOME/micro-servers.yml"

MX_VERSION=`grep version $MX_HOME/version.yml | cut -d':' -f2 | xargs`
echo "+---------------------------------------------------------------------+"
echo "+  Micro-Server v"$MX_VERSION
echo "+---------------------------------------------------------------------+"

MX_JAVA_CLASSPATH="$MX_HOME/src/main/resources"
MX_JAVA_OPTION="-Xms128m -Xmx512m"
debug "MX_JAVA_OPTION=$MX_JAVA_OPTION"
MX_JAVA=$JAVA_HOME/bin/java
STATUS="0"

if [ "x$JAVA_HOME" == "x" ]; then
    #echo "  The property's content in mx.java.home=$MX_JAVA_HOME doesn't exist"
    #echo "  Please make are sure that are you using Java 1.8 and set up thin within $MX_DEPLOY_YML"
    #echo "  Lets try to use the current JAVA_HOME set up for SO"
    MX_JAVA="java"
fi

export JRUBY_OPTS="-J-Dlog4j.debug=true -J-Dlog4j.configuration=file:$MX_HOME/src/main/resources/log4j.properties"

MX_JAVA_PROPERTIES="-Dparent.basedir=$MX_PARENT "
debug "MX_JAVA_PROPERTIES=$MX_JAVA_PROPERTIES"

MX_JAVA_PROPERTIES_CLIENT="-Dparent.basedir=$MX_PARENT "
MCLIENT_NAME="x"
SCRIPT_ACTION="-list-threads"
MSERVER_NAME=`grep mserver.name: $MX_PARENT/micro-env-variables.yml | cut -d':' -f2 | xargs`

while [ $# -gt 0 ]
do
    debug "SHIFT: $1"
    case $1 in
        -server)
            MSERVER_NAME=$2
            debug "MSERVER_NAME: $MSERVER_NAME"
            shift
            ;;
        -client)
            MCLIENT_NAME=$2
            debug "MCLIENT_NAME: $MCLIENT_NAME"
            shift
            ;;
        -start)
            SCRIPT_ACTION="-start"
            debug "SCRIPT_ACTION: $SCRIPT_ACTION"
            ;;
        -stop)
            SCRIPT_ACTION="-stop"
            debug "SCRIPT_ACTION: $SCRIPT_ACTION"
            ;;
        -restart)
            SCRIPT_ACTION="-restart"
            debug "SCRIPT_ACTION: $SCRIPT_ACTION"
            ;;
        -status)
            SCRIPT_ACTION="-status"
            debug "SCRIPT_ACTION: $SCRIPT_ACTION"
            ;;
        -list-threads)
            SCRIPT_ACTION="-list-threads"
            debug "SCRIPT_ACTION: $SCRIPT_ACTION"
            ;;
        -exec)
            SCRIPT_ACTION="-sessionExec"
            ACTION_NAME=$2
            debug "ACTION_NAME: $ACTION_NAME"
            ;;
    esac
    shift
done

MX_PID_NAME="$MX_HOME/logs/$MSERVER_NAME.pid"
debug "MX_PID_NAME: $MX_PID_NAME"

case "$SCRIPT_ACTION" in
    -start)
        echo "==== Starting $MSERVER_NAME"
        MX_JAVA_PROPERTIES="$MX_JAVA_PROPERTIES -Dmserver.name=$MSERVER_NAME "

        debug "$MX_JAVA -server $MX_JAVA_OPTION $MX_JAVA_PROPERTIES -cp $MX_JAVA_CLASSPATH/* org.mx.mserver.MicroServerGateway "
        nohup $MX_JAVA -server $MX_JAVA_OPTION $MX_JAVA_PROPERTIES \
                  -cp "$MX_JAVA_CLASSPATH/*" \
                   org.mx.mserver.MicroServerGateway  \
                   > /dev/null 2>&1 & echo $! > $MX_PID_NAME

        #$MX_JAVA -server $MX_JAVA_OPTION $MX_JAVA_PROPERTIES \
        #           -cp "$MX_JAVA_CLASSPATH/*" \
        #           org.mx.mserver.MicroServerGateway

        sleep 1
        PID=`cat $MX_PID_NAME`
        STATUS=`ps -ef | grep -v grep | grep $PID | wc -l`
        if [ $STATUS -eq 0 ]; then
            echo "==== Error: Micro-Server $MSERVER_NAME wasn't started up, please validate"
            echo "  "
            exit
        else
            echo "==== PID $PID"
            echo "  "
        fi
        ;;
    -stop)
        echo "==== Stopping $MSERVER_NAME"
        MX_JAVA_PROPERTIES_CLIENT="$MX_JAVA_PROPERTIES_CLIENT -Dmclient.chunktype=0 -Dmserver.name=$MSERVER_NAME"
        debug "MX_JAVA_PROPERTIES=$MX_JAVA_PROPERTIES_CLIENT"

        $MX_JAVA -server $MX_JAVA_OPTION $MX_JAVA_PROPERTIES_CLIENT \
                  -cp "$MX_JAVA_CLASSPATH/*" \
                   org.mx.mclient.MicroClient

        sleep 2
        PID=`cat $MX_PID_NAME`
        STATUS=`ps -ef | grep -v grep | grep $PID | wc -l`
        if [ $STATUS -eq 1 ]; then
            echo "==== Micro-Server $MSERVER_NAME wasn't stopped, please validate."
            echo "  "
            exit
        else
            echo "==== Micro-Server $MSERVER_NAME was shutdown"
            echo "  "
            exit
        fi

        ;;
    -restart)
        echo "==== Stopping $MSERVER_NAME"
        MX_JAVA_PROPERTIES_CLIENT="$MX_JAVA_PROPERTIES_CLIENT -Dmclient.chunktype=0 -Dmserver.name=$MSERVER_NAME"
        debug "MX_JAVA_PROPERTIES=$MX_JAVA_PROPERTIES_CLIENT"

        $MX_JAVA -server $MX_JAVA_OPTION $MX_JAVA_PROPERTIES_CLIENT \
                  -cp "$MX_JAVA_CLASSPATH/*" \
                   org.mx.mclient.MicroClient

        sleep 2
        PID=`cat $MX_PID_NAME`
        STATUS=`ps -ef | grep -v grep | grep $PID | wc -l`
        if [ $STATUS -eq 1 ]; then
            echo "==== Micro-Server $MSERVER_NAME wasn't stopped, please validate."
            echo "  "
            exit
        else
            echo "==== Micro-Server $MSERVER_NAME was shutdown"
            echo "==== Starting $MSERVER_NAME"
            MX_JAVA_PROPERTIES="$MX_JAVA_PROPERTIES -Dmserver.name=$MSERVER_NAME "
            debug "$MX_JAVA -server $MX_JAVA_OPTION $MX_JAVA_PROPERTIES -cp $MX_JAVA_CLASSPATH/* org.mx.mserver.MicroServerGateway "
            nohup $MX_JAVA -server $MX_JAVA_OPTION $MX_JAVA_PROPERTIES \
                      -cp "$MX_JAVA_CLASSPATH/*" \
                       org.mx.mserver.MicroServerGateway  \
                       > /dev/null 2>&1 & echo $! > $MX_PID_NAME
            sleep 1
            PID=`cat $MX_PID_NAME`
            STATUS=`ps -ef | grep -v grep | grep $PID | wc -l`
            if [ $STATUS -eq 0 ]; then
                echo "==== Error: Micro-Server $MSERVER_NAME wasn't started up, please validate"
                echo "  "
                exit
            else
                echo "==== PID $PID"
                echo "  "
            fi
        fi

        ;;
    -status)
        debug "STATUS"
        if [ ! -f $MX_PID_NAME ]; then
            echo "==== Micro-Server $MX_MSERVER_NAME is down"
            exit
        else
            PID=`cat $MX_PID_NAME`
            STATUS=`ps -ef | grep -v grep | grep $PID | wc -l`
            if [ $STATUS -eq 1 ]; then
                echo "===== $MX_MSERVER_NAME is up and running"
                #echo "  PID $PID"
            else
                echo "===== $MX_MSERVER_NAME is down"
                echo "  "
            fi
        fi

        ;;
esac


echo " "
