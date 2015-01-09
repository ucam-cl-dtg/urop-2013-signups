#!/usr/bin/python
import sys
from signups import *

def main(obfuscatedId,time,slotDurationMin,numSlots):
    db = DatabaseOps()
    e = Event(obfuscatedId,db)
    e.addRange(time,slotDurationMin,numSlots,1,db)
    db.con.commit()


if __name__ == "__main__":
    main(sys.argv[1],sys.argv[2],int(sys.argv[3]),int(sys.argv[4]))
