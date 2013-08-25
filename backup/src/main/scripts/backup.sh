#!/bin/bash

# this tells bash that if any 'simple' command exits with a non-zero
# return value the script should die too
set -e

HOST=$1
DB=signups
USER=signups
PASSWORD=signups
BACKUPFILE=backup.sql
DATE=`date +%Y-%m-%d-%H:%M:%S`

CMD="pg_dump -p 5433 -h localhost -U $USER $DB"
ssh $HOST -f -L 5433:localhost:5432 sleep 10
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
