package example

import groovy.json.*
import groovyx.net.http.*

import groovy.io.FileType

import java.sql.*

class Hello {
 def jsonSlurper

 Hello() {
  jsonSlurper = new JsonSlurper()
 }

/**
* From a Google page found via keywords "ec2 pricing"
* for on-demand instances
**/
 def instancecostperhour = [
'c3.large':     0.1,
'c4.xlarge':    0.2,
'c4.2xlarge':   0.4,
'g2.8xlarge':   2.6,
'm3.medium':    0.11,
'm3.large':     0.11,
'm3.xlarge':    0.21,
'm4.large':     0.11,
'm4.xlarge':    0.21,
'm4.2xlarge':   0.43, //week=0.43*24*7=72
'm4.4xlarge':   0.86,
'r3.2xlarge':   0.53,
'r3.xlarge':    0.33,
't2.micro':     0.01,
't2.small':     0.02,
't2.medium':    0.05,
't2.large':     0.09
]

 ArrayList getNamesOfCapturedFiles(dirname) {
  //dirname is a directory with subdirectories
  //the subdirectories are 'us-east-1' and 'us-west-2'
  def files = []
  def dir = new File(dirname)
  dir.eachFileRecurse (FileType.FILES) { file ->
   files << file
  }
  def sortedfiles = files.sort()
 }

 def interpretNameOfCapturedFile(dirname, s) {
   def secondsInPreviousMonthsIn2017 = [
    '01':  0*(24*60*60),
    '02': 31*(24*60*60),
    '03': 59*(24*60*60),
    '04': 90*(24*60*60),
    '05':120*(24*60*60),
    '06':151*(24*60*60),
    '07':181*(24*60*60),
    '08':212*(24*60*60),
    '09':243*(24*60*60),
    '10':273*(24*60*60),
    '11':304*(24*60*60),
    '12':334*(24*60*60)
   ]

   def sskey = s.toString()
   def region = sskey["$dirname/".size().."$dirname/us-east-1".size()-1] 
   def timestamp = sskey[
    "$dirname/us-east-1/".size()..
    "$dirname/us-east-1/".size()+
     '2017_02_10_08_01_01'.size()-1]
   def timestampYear = timestamp[0..3]
   def timestampMonth = timestamp[5..6]
   def timestampDay = timestamp[8..9]
   def timestampHour = timestamp[11..12]
   def timestampMinute = timestamp[14..15]
   def timestampSecond = timestamp[17..18]

   def timestampSecondsInto2017 = 
    secondsInPreviousMonthsIn2017[timestampMonth]+
    timestampDay.toInteger()*24*60*60 + 
    timestampHour.toInteger()*60*60 + 
    timestampMinute.toInteger()*60 + 
    timestampSecond.toInteger()
   [
    region: region,
    timestamp: timestamp,
    timestampYear: timestampYear,
    timestampMonth: timestampMonth,
    timestampDay: timestampDay,
    timestampHour: timestampHour,
    timestampMinute: timestampMinute,
    timestampSecond: timestampSecond,
    timestampSecondsInto2017: timestampSecondsInto2017
   ]
 }

 void interpretEC2jsonAndPrint(f, t) {
   //The interpreted json values are printed here as a convenience
   // rather than returning them within an array.
   def object = jsonSlurper.parseText(t) 
   for (def i = 0; i < object.Reservations.size(); i++) {
    def instanceId   = object.Reservations[i].Instances[0].InstanceId
    def instanceType = object.Reservations[i].Instances[0].InstanceType
    def keyName      = object.Reservations[i].Instances[0].KeyName
    def state        = object.Reservations[i].Instances[0].State.Name
    def projectValue = getTagProjectValue(object.Reservations[i].Instances[0].Tags)

    print   "${f.region},${f.timestamp},${f.timestampSecondsInto2017},"
    print   "${f.timestampYear},${f.timestampMonth},${f.timestampDay},"
    print   "${f.timestampHour},${f.timestampMinute},${f.timestampSecond},"
    println "$instanceId,$instanceType,$keyName,$state,$projectValue"
   }
 }

 void step1() {
  def sortedfiles = getNamesOfCapturedFiles('captured')

  sortedfiles.each { s ->
   HashMap f = interpretNameOfCapturedFile('captured', s)
   String t = new File(s.toString()).text
   interpretEC2jsonAndPrint(f, t)
  }
 }

 String getTagProjectValue(tag) {
  def projectvalue = 'untagged'

  if (tag) {
   for (int i=0; i<tag.size(); i++) {
    if (tag[i].Key == 'Project') {
     projectvalue = tag[i].Value
    }
   }
  }

  projectvalue
 }

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

 void step2(inputfilename) {
  Class.forName("org.h2.Driver");
  Connection conn = DriverManager.
  getConnection("jdbc:h2:~/cat", "sa", "");

  def stmt2 = conn.createStatement()
  try {
   String sql2 = "drop table cat.public.step1"
   def x2 = stmt2.execute(sql2)
  }
  catch (e) {
  }

  def stmt3 = conn.createStatement()
  String sql3 = '' +
   'create table cat.public.step1 (' +
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
  String sql4 = "select * from step1 order by instanceid, xtimestamp"
  ResultSet rs4 = stmt4.executeQuery(sql4);

  print   "region,instanceid,xtimestamp,savepreviousxtimestamp,"
  print   "xtimestampSecondsInto2017,xyear,xmonth,xday,xhour,xminute,xsecond,"
  print   "xsavepreviousxtimestampSecondsInto2017,secondsPassed,type,"
  println "secgrp,status,projectValue,costperthisline"

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

   print   "$region,$instanceid,$xtimestamp,$savepreviousxtimestamp,"
   print   "$xtimestampSecondsInto2017,$xyear,$xmonth,$xday,$xhour,$xminute,$xsecond,"
   print   "$xsavepreviousxtimestampSecondsInto2017,$secondsPassed,$type,"
   println "$secgrp,$status,$projectValue,$costperthisline"

   previousinstanceid = instanceid
   previousxtimestamp = xtimestamp 
  }

  conn.close();
 }

 void step3(inputfilename) {

  /**
  * cat is an arbitrary name
  */
  Class.forName("org.h2.Driver");
  Connection conn = DriverManager.

  // Use the default username/password:
  //  - the data is not sensitive
  //  - the h2 console is restricted by IP
  getConnection("jdbc:h2:tcp://localhost/~/cat", "sa", "");
  //getConnection("jdbc:h2:~/cat", "sa", "");

  /**
  * Only one table is used for ad hoc analysis
  */
  def stmt2 = conn.createStatement()
  try {
   String sql2 = "drop table cat.public.step3"
   def x2 = stmt2.execute(sql2)
  }
  catch (e) {
   println "no table cat.public.step3 is available to drop"
  }

  /**
  * Use h2's CSVREAD
  */
  def stmt3 = conn.createStatement()
  String sql3 = '' +
   'create table cat.public.step3 (' +
   ' region varchar(100),' +
   ' instanceid varchar(100),' +
   ' xtimestamp varchar(100),' + 
   ' savepreviousxtimestamp varchar(100),' + 
   ' xtimestampSecondsInto2017 varchar(100),' +
   ' xyear int,' +
   ' xmonth int,' +
   ' xday int,' +
   ' xhour int,' +
   ' xminute int,' +
   ' xsecond int,' +
   ' xsavepreviousxtimestampSecondsInto2017 int,' +
   ' secondsPassed int,' +
   ' type varchar(100),' +
   ' secgrp varchar(100),' +
   ' status varchar(100),' +
   ' projectValue varchar(100),' +
   ' costperthisline decimal)' +
   " as select * from CSVREAD('deleteme4')"
   .replaceAll('deleteme4',inputfilename)

   def x3 = stmt3.execute(sql3)

  conn.close();
 }

}

def h = new Hello()
if ((args[0]) == 'step1') {
 h.step1()
 //step1 writes to stdout
}
if ((args[0]) == 'step2') {
 def inputfilename = args[1]
 h.step2(inputfilename)
 //step2 writes to stdout
}
if ((args[0]) == 'step3') {
 def inputfilename = args[1]
 h.step3(inputfilename)
 //step3 writes to h2
}
