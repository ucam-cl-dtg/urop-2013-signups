#!/usr/bin/python

from signups import *
import dateutil.parser
import csv
import sys

def main(filename):
    db = DatabaseOps()

    with open(filename) as f:
        r = csv.reader(f)
        w = csv.writer(sys.stdout)
        for row in list(r)[1:]:
            (unit,course,desc,room,number,deadline) = map(lambda x:x.strip(),row)
            if unit:
                id = create("%s: %s" % (unit,course),desc,room,int(number),deadline,db)
                w.writerow(row+["http://otter.cl.cam.ac.uk/signups/events/%s" % id])

    db.con.commit()

def create(name,desc,room,number,deadline,db):
    print >> sys.stderr,"Creating %s" % name
    e = Event.create(name,
                     desc,
                     "acr31",
                     deadline,
                     "William Gates Building",
                     room,
                     "manual",
                     False,
                     True,
                     "Course",
                     db)
    for a in range(0,number):
        e.addSlot(None,db)

    return e.obfuscatedId


if __name__ == "__main__":
    main(sys.argv[1])
