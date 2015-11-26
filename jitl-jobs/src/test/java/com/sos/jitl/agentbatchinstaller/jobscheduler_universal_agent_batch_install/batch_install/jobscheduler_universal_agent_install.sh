#!/bin/sh 
# Copyright (c) 2015 SOS GmbH, Berlin, Germany.
# All rights reserved.
export TERM=dumb
  clear
  echo "+---------------------------------------------------------+"
  echo "*     JobScheduler Univeral Agent                         *"
  echo "*     SOS Software- und Organisations Service GmbH        *" 
  echo "*     Giesebrechtstr. 15                                  *"
  echo "*     10629 Berlin                                        *"
  echo "*     ----------------------------------------------      *"
  echo "*     info@sos-berlin.com                                 *"
  echo "*     http://www.sos-berlin.com                           *"
  echo "+---------------------------------------------------------+"
  echo ""
  echo ""
  echo "+---------------------------------------------------------+"
  echo "*  This will install JobScheduler Universal Agent now ... *"
  echo "+---------------------------------------------------------+"
 
# -----------------------------------------------
cd `dirname $0`
  
SETUP_WORKING_DIR=`pwd` 
SETUP_INSTALL_PATH=""
SHOW_EXTRACTED_FILESNAMES=0
SETUP_LOG_FILE="$SETUP_WORKING_DIR/jobscheduler_universal_install.log"
SETUP_ERR=0
 


# ----------------------------------------------------------------- 
log_write() {
  case "$1" in
      0  ) msg_hint="[info]   ";;
      1  ) msg_hint="[warning]";;
      2  ) msg_hint="[error]  ";;
      3  ) msg_hint="[fatal]  ";;
      *  ) msg_hint="[info]   ";;
  esac
  timestamp=`date "+%Y-%m-%d %T"`
  msg="$timestamp   $msg_hint   $2"
  echo "$msg"
  echo "$msg">>"$SETUP_LOG_FILE"
}

show_usage() {
  echo "`basename $1` will install the 'JobScheduler Universal Agent' at [install_dir]." 
  echo "install_dir=Path with the JobScheduler Universal Agent"
  echo
  echo "Usage: `basename $1` [OPTION]"
  echo
  echo "Samples:"
  echo "  `basename $1` -co  -d/home/jobscheduler  -p4445 "      
  echo 
  echo "Options:"
  echo "  -h, --help                          | Shows this usage"
  echo "  -s, --show                          | show all extracted filenames"
  echo "  -f  --installation_file             | File name of the JobScheduler Universal Agent installation file"
  echo "  -d  --install_dir                   | Path of the JobScheduler Universal Agent installation"
  echo "  -p  --port                          | Port of JobScheduler Universal Agent. Default=4445"
 
}
 
# ----------------------------------------------------------------------------usage
if [ $# -eq 0 ]
then
  show_usage $0
  exit 65
fi 



# --------------------------------------------
     

for arg in "$@"
do
  long_opt=0
  echo "`expr match $arg '\(..\)'`" 
  case "`expr match $arg '\(..\)'`" in
    "-h"        )  show_usage $0; exit 64;;
    "-d"        )  SETUP_INSTALL_PATH="`expr match $arg '-d\(.*\)'`";;
    "-f"        )  SETUP_INSTALLATION_FILE="`expr match $arg '-f\(.*\)'`";;
    "-p"        )  UNIVERSAL_AGENT_PORT="`expr match $arg '-p\(.*\)'`";;
    "-s"        )  SHOW_EXTRACTED_FILESNAMES="`expr match $arg '-d\(.*\)'`";;
    "--"        )  long_opt=1;;
  esac 
  
  if [ "$long_opt" -eq 1 ]
  then
  case `expr match "$arg" '\(--[^=]*\)'` in
    "--show_extracted_filenames"    )  SHOW_EXTRACTED_FILESNAMES=`expr match "$arg" '--show_extracted_filenames=\(.*\)'`;;
    "--help"    )  show_usage $0; exit 64;;
    "--install_dir"    )  SETUP_INSTALL_PATH=`expr match "$arg" '--install_dir=\(.*\)'`;;
    "--installation_file"    )  SETUP_INSTALATION_FILE=`expr match "$arg" '--installation_file=\(.*\)'`;;
    "--port"    )  UNIVERSAL_AGENT_PORT=`expr match "$arg" '--port=\(.*\)'`;;
  esac
  fi

done

 
# ---------------

  
check_dir(){
     SETUP_INSTALL_PATH="$1"
     echo SETUP_INSTALL_PATH="$SETUP_INSTALL_PATH"

     if [ -d "$SETUP_INSTALL_PATH" ]
     then
       if [ -r "$SETUP_INSTALL_PATH" ]
       then
         cd "$SETUP_INSTALL_PATH"
         SETUP_INSTALL_PATH=`pwd`
         chmod 755 "$SETUP_INSTALL_PATH" 2>/dev/null
         SETUP_ERR=$?
       else
         log_write 2 "$SETUP_INSTALL_PATH is not readable."
         SETUP_ERR=1
       fi
     else
       echo create $SETUP_INSTALL_PATH

       mkdir -m755 -p "$SETUP_INSTALL_PATH" 2>/dev/null
       SETUP_ERR=$?
       if [ $SETUP_ERR -eq 0 ]
       then
      
         cd  "$SETUP_INSTALL_PATH"
         SETUP_INSTALL_PATH=`pwd`
       else
         log_write 2 "$SETUP_INSTALL_PATH can not create."
         SETUP_ERR=1 
       fi
     fi
     
     
     cd  "$SETUP_WORKING_DIR"
     if [ $SETUP_ERR -ne 0 ]
     then
        log_write 2 "You haven't the necessary rights on the installation directory $SETUP_INSTALL_PATH."
     exit $SETUP_ERR
fi
}

tarx (){
  if [ -f "$1.gz" ]
  then
     gzip -fd $1.gz
  fi
  
  if [ ! -d "$2" ]
  then
      mkdir $2
  fi
 
  cd  $2
  tar $tarpar $SETUP_WORKING_DIR/$1
  cd  $SETUP_WORKING_DIR
} 

tarpar="-xf "

if [ "$SHOW_EXTRACTED_FILESNAMES" = "1" ]
then
   tarpar="-xvf "
fi


SETUP_INSTALL_PATH="$SETUP_INSTALL_PATH"

echo SETUP_INSTALL_PATH=$SETUP_INSTALL_PATH
check_dir $SETUP_INSTALL_PATH
  

# Installing
log_write 0 "Installing --> $SETUP_INSTALL_PATH"
tarx  $SETUP_INSTALLATION_FILE  $SETUP_INSTALL_PATH 

if [ -z "$UNIVERSAL_AGENT_PORT" ]
then
$UNIVERSAL_AGENT_PORT=4445
fi

mv `dirname $0`/jobscheduler_agent_$UNIVERSAL_AGENT_PORT.sh $SETUP_INSTALL_PATH/jobscheduler_agent/bin 
chmod a+x  $SETUP_INSTALL_PATH/jobscheduler_agent/bin/jobscheduler_agent_$UNIVERSAL_AGENT_PORT.sh   
log_write 0 "Start JobScheduler Universal Agent"
log_write 0 $SETUP_INSTALL_PATH/jobscheduler_agent/bin/jobscheduler_agent_$UNIVERSAL_AGENT_PORT.sh start
$SETUP_INSTALL_PATH/jobscheduler_agent/bin/jobscheduler_agent_$UNIVERSAL_AGENT_PORT.sh start 1 > /dev/null

exit $?
