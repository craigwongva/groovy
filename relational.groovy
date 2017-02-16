package example

import groovy.json.*
import groovyx.net.http.*

import groovy.io.FileType

import java.sql.*

class Relational {
 int secondsInto2017(timestamp) {
   def NUM_SECONDS_IN_A_DAY = 24*60*60

   if (timestamp == 'n/a') {
    return -1
   }

   def timestampYear = timestamp[0..3]
   def timestampMonth = timestamp[5..6]
   def timestampDay = timestamp[8..9]
   def timestampHour = timestamp[11..12]
   def timestampMinute = timestamp[14..15]
   def timestampSecond = timestamp[17..18]
   def secondsInPreviousMonthsIn2017 = [
    '01':  0*NUM_SECONDS_IN_A_DAY,
    '02': 31*NUM_SECONDS_IN_A_DAY,
    '03': 59*NUM_SECONDS_IN_A_DAY,
    '04': 90*NUM_SECONDS_IN_A_DAY,
    '05':120*NUM_SECONDS_IN_A_DAY,
    '06':151*NUM_SECONDS_IN_A_DAY,
    '07':181*NUM_SECONDS_IN_A_DAY,
    '08':212*NUM_SECONDS_IN_A_DAY,
    '09':243*NUM_SECONDS_IN_A_DAY,
    '10':273*NUM_SECONDS_IN_A_DAY,
    '11':304*NUM_SECONDS_IN_A_DAY,
    '12':334*NUM_SECONDS_IN_A_DAY
   ]
   def timestampSecondsInto2017 = 
    timestampDay.toInteger()*NUM_SECONDS_IN_A_DAY + 
    timestampHour.toInteger()*60*60 + 
    timestampMinute.toInteger()*60 + 
    timestampSecond.toInteger()
 }

 void foo(inputfilename) {
  Class.forName("org.h2.Driver");
  Connection conn = DriverManager.
  getConnection("jdbc:h2:~/cat", "sa", "");

  def stmt2 = conn.createStatement()
  try {
   String sql2 = "drop TABLE CAT.PUBLIC.QZ"
   def x2 = stmt2.execute(sql2)
  }
  catch (e) {
  }

  def stmt3 = conn.createStatement()
  String sql3 = '' +
   'create table cat.public.qz (' +
   ' region varchar(100),' +
   ' xtimestamp varchar(100),' + 
   ' xseconds2017 varchar(100),' +
   ' xyear varchar(100),' +
   ' xmonth varchar(100),' +
   ' xday varchar(100),' +
   ' xhour varchar(100),' +
   ' xminute varchar(100),' +
   ' xsecond varchar(100),' +
   ' instanceid varchar(100),' +
   ' type varchar(100),' +
   ' secgrp varchar(100),' +
   ' status varchar(100),' +
   ' projectValue varchar(100))' +
   " as select * from CSVREAD('deleteme4')"
   .replaceAll('deleteme4',inputfilename)

   def x3 = stmt3.execute(sql3)

  def stmt4 = conn.createStatement();
  String sql4 = "select * from qz order by instanceid, xtimestamp"
  ResultSet rs4 = stmt4.executeQuery(sql4);

  println "region,instanceid,xtimestamp,savepreviousxtimestamp,xtimestampSecondsInto2017,xyear,xmonth,xday,xhour,xminute,xsecond,xsavepreviousxtimestampSecondsInto2017,secondsPassed,type,secgrp,status,projectValue,costperthisline"

  def i=0
  def previousinstanceid = 'i-00000000'
  def previousxtimestamp = 'n/a'
  def savepreviousxtimestamp
  while(rs4.next() && i++ <= 2000000){
   String region       = rs4.getString("region");
   String xtimestamp   = rs4.getString("xtimestamp");
   String xyear        = rs4.getString("xyear")
   String xmonth       = rs4.getString("xmonth")
   String xday         = rs4.getString("xday")
   String xhour        = rs4.getString("xhour")
   String xminute      = rs4.getString("xminute")
   String xsecond      = rs4.getString("xsecond")
   String instanceid   = rs4.getString("instanceid");
   String type         = rs4.getString("type");
   String secgrp       = rs4.getString("secgrp");
   String status       = rs4.getString("status");
   String projectValue = rs4.getString("projectValue");

   if (instanceid != previousinstanceid) {
    savepreviousxtimestamp = 'n/a' //s/m: this savexxx name isn't really helpful
   }
   else {
    savepreviousxtimestamp = previousxtimestamp
   }

   def xtimestampSecondsInto2017 = secondsInto2017(xtimestamp)
   def xsavepreviousxtimestampSecondsInto2017 = secondsInto2017(savepreviousxtimestamp)
   int secondsPassed
   if (xsavepreviousxtimestampSecondsInto2017 == -1) {
    secondsPassed = 0
   }
   else {
    secondsPassed = xtimestampSecondsInto2017 - xsavepreviousxtimestampSecondsInto2017
   }
  
   def costperhour  = instancecostperhour[type]
   def costpersecond = costperhour/(60*60)
   def costperthisline
   if (status == 'running') {
    costperthisline = secondsPassed * costpersecond
   }
   else {
    costperthisline = 0
   }
   println "$region,$instanceid,$xtimestamp,$savepreviousxtimestamp,$xtimestampSecondsInto2017,$xyear,$xmonth,$xday,$xhour,$xminute,$xsecond,$xsavepreviousxtimestampSecondsInto2017,$secondsPassed,$type,$secgrp,$status,$projectValue,$costperthisline"

   previousinstanceid = instanceid
   previousxtimestamp = xtimestamp 
  }

  conn.close();
 }

/**
* From a Google page found via keywords "ec2 pricing"
* for on-demand instances
**/
 def instancecostperhour = [
'c3.large':	0.1,
'c4.xlarge':	0.2,
'c4.2xlarge':	0.4,
'g2.8xlarge':	2.6,
'm3.medium':	0.11,
'm3.large':	0.11,
'm3.xlarge':	0.21,
'm4.large':	0.11,
'm4.xlarge':	0.21,
'm4.2xlarge':	0.43,
'm4.4xlarge':	0.86,
'r3.2xlarge':	0.53,
'r3.xlarge':	0.33,
't2.micro':	0.01,
't2.small':	0.02,
't2.medium':	0.05,
't2.large':	0.09
]
}

def h = new Relational()
h.foo(args[0])
