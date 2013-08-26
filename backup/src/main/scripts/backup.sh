#!/bin/bash

get_port() {
    # strip out username if there
    HOST=`echo $1 | sed -e "s/^.*@//"`
    echo $(( `dig +short $HOST | tail -n1 | sed -e "s/\.//g"` % 55535 + 10000 ))
}

# this tells bash that if any 'simple' command exits with a non-zero
# return value the script should die too
set -e

HOST=$1
DB=signups
USER=signups
PASSWORD=signups
BACKUPFILE=backup.sql
DATE=`date +%Y-%m-%d-%H:%M:%S`
PORT=`get_port $HOST`

CMD="pg_dump -p $PORT -h localhost -U $USER $DB"
ssh $HOST -f -L $PORT:localhost:5432 sleep 10
export PGPASSWORD=$PASSWORD
$CMD > $BACKUPFILE
export PGPASSWORD=

cat > README <<EOF
Time-taken: $DATE
Database host: $HOST
User: $USER
Database: $DB
Backup command: $CMD
EOF
