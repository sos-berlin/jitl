#! /bin/sh
#  ------------------------------------------------------------
#  Company: Softwae- und Organisations-Service GmbH
#  Pupose: Startscript for JobScheduler Agent
#  ------------------------------------------------------------

### BEGIN INIT INFO
# Povides:          JobSchedulerAgent_4445
# Requied-Start:    $syslog $remote_fs
# Requied-Stop:     $syslog $remote_fs
# Default-Stat:     3 5
# Default-Stop:      0 1 2 6
# Desciption:       Start JobScheduler Agent
### END INIT INFO


### SETTINGS #############################################

# This vaiable has to point to the installation path of 
# the JobSchedule Agent. If this variable not defined 
# then the paent directory of this startscript is used.
### NOTE: 
# This vaiable is mandatory if this script is used for a  
# JobSchedule Agent service in /etc/init.d to find the 
# installation of the JobSchedule Agent.
#
SCHEDULER_HOME=${SCHEDULER_HOME}

# Set the use for the JobScheduler Agent. Otherwise the 
# curent logged user is used. This variable has to be set 
# if this scipt is used for autostart to avoid that the 
# JobSchedule Agent is started with the 'root' user.
#
SCHEDULER_USER=${SCHEDULER_USER}

# The http pot of the JobScheduler Agent can be set here,
# as command line option -http-pot (see usage) or as
# envionment variable. Otherwise the above default port 
# is used.
# The command line option -http-pot beats the environment 
# vaiable SCHEDULER_HTTP_PORT and the environment variable 
# SCHEDULER_HTTP_PORT beats the default pot from 
# SCHEDULER_AGENT_DEFAULT_HTTP_PORT.
### NOTE:
# If you stat the JobScheduler Agent with the command line 
# option -http-pot then you must enter -http-port for 
# stop, status, estart too (see usage). It's recommended 
# to set this envionment variable instead.
#
SCHEDULER_HTTP_PORT=${SCHEDULER_HTTP_PORT}

# Could be used fo indicating which network interfaces the 
# JobSchedule Agent should listen to.
# Othewise it listens to all available network interfaces.
#
#SCHEDULER_IP_ADDRESS=

# Set the diectory where the JobScheduler Agent log file 
# is ceated. The default is SCHEDULER_HOME/logs
#
SCHEDULER_LOG_DIR=${SCHEDULER_LOG_DIR}


# Set the diectory where the JobScheduler Agent pid file 
# is ceated. The default is SCHEDULER_LOG_DIR
#
#SCHEDULER_PID_FILE_DIR=

# Set the location of a scipt which is called by the 
# JobSchedule Agent to kill a process and it's children.
# The default is SCHEDULER_HOME/bin/jobschedule_agent_kill_task.sh
#
#SCHEDULER_KILL_SCRIPT=

# Actually JAVA_HOME is aleady set. If you want to use 
# anothe Java environment then you can set it here. If  
# no JAVA_HOME is set then the Java fom the path is used
# (see 'which java').
#
#JAVA_HOME=

# With Java 1.8 the initial memoy allocation has changed, 
# fo details see https://kb.sos-berlin.com/x/aIC9
# As a esult on start-up of the JobScheduler Agent an 
# excessive amount of vitual memory is assigned by Java.  
# The envionment variable JAVA_OPTIONS can use to apply 
# memoy settings such as '-Xms100m' (default).
#
JAVA_OPTIONS=${JAVA_OPTIONS}

##########################################################

. "$SCHEDULER_HOME/bin/jobscheduler_agent.sh"