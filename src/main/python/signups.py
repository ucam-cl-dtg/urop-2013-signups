from uuid import uuid4
import psycopg2
import dateutil.parser
import datetime

class Event:

    def __init__(self,obfuscatedId,dbOps):
        dbOps.cur.execute("SELECT id from events where obfuscatedid=%s",[obfuscatedId])
        self.eventId = dbOps.cur.fetchone()[0]
        self.obfuscatedId = obfuscatedId
    
    @classmethod
    def create(cls,title,description,owner,expirydate,location,room,sheettype,freeform_editable,dos_visibility,type,dbOps):
        (eventId,obfuscatedId) = dbOps.createEvent(title,description,owner,expirydate,location,room,sheettype,freeform_editable,dos_visibility,type)
        return Event(obfuscatedId,dbOps)

    def addSlot(self,time,dbOps):
        dbOps.addSlot(self.eventId,time)

    def setExpiryToEnd(self,dbOps):
        dbOps.setExpiryToEnd(self.eventId)

    def addRange(self,startTime,slotLengthMinutes,numSlots,numCols,dbOps):
        for col in range(0,numCols):
            start = dateutil.parser.parse(startTime)
            for i in range(0,numSlots):
                self.addSlot(start,dbOps)
                start += datetime.timedelta(minutes=slotLengthMinutes)

class DatabaseOps:
    con = None
    cur = None

    def __init__(self):
        self.con = psycopg2.connect(dbname='signups', user='signups',password='signups',host='localhost') 
        self.cur = self.con.cursor()
    
    def nextVal(self,sequence):
        self.cur.execute("SELECT nextval('%s')" % (sequence))
        ids = self.cur.fetchone()
        if ids and ids[0]:
            return ids[0]
        return 1

    def now(self):
        return time.strftime("%Y-%m-%d %H:%M")

    def newObfuscatedId(self):
        id = str(uuid4()).replace('-','')[:8]
        self.cur.execute("SELECT obfuscatedid from events where obfuscatedid=%s",[id])
        ids = self.cur.fetchone()
        if ids and ids[0]:
            return self.newObfuscatedId()
        else:
            return id

    def createEvent(self,title,description,owner,expirydate,location,room,sheettype,freeform_editable,dos_visibility,type):
        id = self.nextVal("log_seq")
        obfuscatedid = self.newObfuscatedId()
        self.cur.execute("INSERT INTO events(id,dos_visibility,expirydate,location,obfuscatedid,room,sheettype,title,user_crsid,description,freeform_editable) values (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)",[id,dos_visibility,expirydate,location,obfuscatedid,room,sheettype,title,owner,description,freeform_editable])
        typeid = self.nextVal("log_seq")
        self.cur.execute("INSERT INTO types(id,name,event_id) VALUES (%s,%s,%s)",[typeid,type,id])
        return (id,obfuscatedid)

    def addSlot(self,eventId,time):
        self.cur.execute("SELECT id from rows where event_id=%s and row_date=%s",[eventId,time]);
        result = self.cur.fetchone()
        if not (result and result[0]):
            self.cur.execute("SELECT id from types where event_id = %s",[eventId])
            type_id = self.cur.fetchone()[0]
            rowid = self.nextVal("log_seq")
            self.cur.execute("INSERT into rows(id,row_date,event_id,type_id) values (%s,%s,%s,%s)",[rowid,time,eventId,type_id])
        else:
            rowid = result[0]
        slotid = self.nextVal("log_seq")
        self.cur.execute("INSERT into slots(id,row_id) values (%s,%s)",[slotid,rowid])
        return slotid

    def setExpiryToEnd(self,eventId):
        self.cur.execute("SELECT row_date from rows where event_id=%s order by row_date desc",[eventId])
        rows = self.cur.fetchall()
        if len(rows) == 1:
            self.cur.execute("UPDATE events set expirydate = %s where id = %s",[rows[0][0],eventId])
        else:
            d1 = rows[0][0] #dateutil.parser.parse(rows[0][0])
            d2 = rows[1][0] # dateutil.parser.parse(rows[1][0])
            interval = d1-d2
            expiry = datetime.datetime.strftime(d1+interval,"%Y-%m-%d %H:%M:%S")
            self.cur.execute("UPDATE events set expirydate = %s where id = %s",[expiry,eventId])

    def deleteEvent(self,obfuscatedId):
        self.cur.execute("select id from events where obfuscatedid=%s",[obfuscatedId])
        id = self.cur.fetchone()[0]
        self.cur.execute("DELETE from slots where row_id in (select id from rows where event_id=%s)",[id]);
        self.cur.execute("DELETE from rows where event_id = %s",[id])
        self.cur.execute("DELETE from types where event_id = %s",[id])
        self.cur.execute("DELETE from events where id=%s",[id])
