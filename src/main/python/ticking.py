#!/usr/bin/python

from signups import *
import dateutil.parser

def main():
    db = DatabaseOps()
    
    create("2015-01-15","A,B,C,D,E,F","",db)
    create("2015-01-15","","G,H",db)

    db.con.commit()

def create(sessionDate, javaTickers, mlTickers,db):
    titlePrefix = "1A Ticking Session"
    javaTickers = javaTickers.split(",") if len(javaTickers) > 0 else []
    mlTickers = mlTickers.split(",") if len(mlTickers) > 0 else []
    allTickers = list(javaTickers)
    for a in mlTickers:
        if not a in javaTickers:
            allTickers.append(a)
    allTickers = sorted(allTickers)

    description = "Sign up with your ticker using the form below. Each column corresponds to Ticker %s in order." % ",".join(allTickers)
    if len(javaTickers) > 0:
        description+= " Tickers %s are for Java only." % ",".join(javaTickers)
    if len(mlTickers) > 0:
        description+= " Tickers %s are for ML only." % ",".join(mlTickers)

    e = Event.create(titlePrefix +" / Session 1",description,"acr31",None,"William Gates Building","Intel Lab","datetime",False,True,"Ticking",db)
    e.addRange("%s 14:00" % sessionDate,5,24,len(allTickers),db)
    e.setExpiryToEnd(db)
    print "Session 1: http://otter.cl.cam.ac.uk/signups/events/%s" % (e.obfuscatedId)
    e = Event.create(titlePrefix +" / Session 2",description,"acr31",None,"William Gates Building","Intel Lab","datetime",False,True,"Ticking",db)
    e.addRange("%s 16:00" % sessionDate,5,24,len(allTickers),db)
    e.setExpiryToEnd(db)
    print "Session 2: http://otter.cl.cam.ac.uk/signups/events/%s" % (e.obfuscatedId)


if __name__ == "__main__":
    main()
