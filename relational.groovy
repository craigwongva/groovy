package example

import groovy.json.*
import groovyx.net.http.*

import groovy.io.FileType

import java.sql.*

class Relational {
 int secondsInto2017(timestamp) {

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
   def timestampSecondsInto2017 = 
    timestampDay.toInteger()*24*60*60 + 
    timestampHour.toInteger()*60*60 + 
    timestampMinute.toInteger()*60 + 
    timestampSecond.toInteger()
 }

 void foo() {
  Class.forName("org.h2.Driver");
  Connection conn = DriverManager.
  getConnection("jdbc:h2:~/cat", "sa", "");
/*
  //STEP 4: Execute a query
  def stmt = conn.createStatement();

  String sql = "SELECT table_catalog, table_schema, table_name FROM information_schema.tables";
  ResultSet rs = stmt.executeQuery(sql);

  //STEP 5: Extract data from result set
  while(rs.next()){
   String table_catalog  = rs.getString("table_catalog");
   String table_schema   = rs.getString("table_schema");
   String table_name     = rs.getString("table_name");
   //int age = rs.getInt("age");

   println "$table_catalog $table_schema $table_name "
  }
  rs.close();
*/
/* works great 11:15am
  def stmt2 = conn.createStatement()
  String sql2 = "drop TABLE CAT.PUBLIC.QZ"
  def x2 = stmt2.execute(sql2)
  //println "x2 is $x2"


  def stmt3 = conn.createStatement()
  //String sql3 = "CREATE TABLE CAT.PUBLIC.QZ (ID INT PRIMARY KEY, NAME VARCHAR(255)) AS SELECT * FROM CSVREAD('foo2.csv')"
  String sql3 = "create table cat.public.qz (region varchar(100),xtimestamp varchar(100),xseconds2017 varchar(100),xyear varchar(100),xmonth varchar(100),xday varchar(100),xhour varchar(100),xminute varchar(100),xsecond varchar(100),instanceid varchar(100),type varchar(100),secgrp varchar(100),status varchar(100)) as select * from CSVREAD('deleteme4')"
  def x3 = stmt3.execute(sql3)
  println "x3 is $x3"
*/

  println "I'm starting the select order by"
  def stmt4 = conn.createStatement();
  String sql4 = "select * from qz order by instanceid, xtimestamp"
  ResultSet rs4 = stmt4.executeQuery(sql4);

  //STEP 5: Extract data from result set
  def i=0
  def previousinstanceid = 'i-00000000'
  def previousxtimestamp = 'n/a'
  def savepreviousxtimestamp
  while(rs4.next() && i++ <= 200){
   String region       = rs4.getString("region");
   String xtimestamp   = rs4.getString("xtimestamp");
   String instanceid   = rs4.getString("instanceid");
   String type         = rs4.getString("type");
   String secgrp       = rs4.getString("secgrp");
   String status       = rs4.getString("status");

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
  
   println "$region,$instanceid,$xtimestamp,$savepreviousxtimestamp,$xtimestampSecondsInto2017,$xsavepreviousxtimestampSecondsInto2017,$secondsPassed,$type,$secgrp,$status"

   previousinstanceid = instanceid
   previousxtimestamp = xtimestamp 
  }

  //rs.close();
  conn.close();
 }

 void capture(region) {
  def s = "aws ec2 describe-instances --region $region".execute().text
  def timestamp = 'date +"%Y_%m_%d_%H_%M_%S"'.execute().text
   .replaceAll('"','')
   .replaceAll('\n','')

  def f = new File("captured/${timestamp}.json")
  f.write(s)
 }

 void analyze() {
  def files = []

  //println "I'm getting file list from /captured"
  def dir = new File("captured")
  dir.eachFileRecurse (FileType.FILES) { file ->
   files << file
  }
  def sortedfiles = files.sort()

  //println "I'm building hash table of form [filename:json]"
  def snapshot = [:]
  sortedfiles.each { s ->
   def t = new File(s.toString()).text
   snapshot[s.toString()] = t
   //println s.toString()
  }

  //println "I'm visiting each json:"
  snapshot.each { ss ->
   def region = ss.key['captured/'.size()..'captured/us-east-1'.size()-1]
   def timestamp = ss.key[
    'captured/us-east-1/'.size()..
    'captured/us-east-1/'.size()+
     '2017_02_10_08_01_01'.size()-1]
   def timestampYear = timestamp[0..3]
   def timestampMonth = timestamp[5..6]
   def timestampDay = timestamp[8..9]
   def timestampHour = timestamp[11..12]
   def timestampMinute = timestamp[14..15]
   def timestampSecond = timestamp[17..18]
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
   def timestampSecondsInto2017 = 
    timestampDay.toInteger()*24*60*60 + 
    timestampHour.toInteger()*60*60 + 
    timestampMinute.toInteger()*60 + 
    timestampSecond.toInteger()

  def snap = snapshot[ss.key]

  def jsonSlurper = new JsonSlurper()
  def object = jsonSlurper.parseText(snap)
  for (def i = 0; i < object.Reservations.size(); i++) {
   def instanceId   = object.Reservations[i].Instances[0].InstanceId
   def instanceType = object.Reservations[i].Instances[0].InstanceType
   def keyName      = object.Reservations[i].Instances[0].KeyName
   def state        = object.Reservations[i].Instances[0].State.Name
   println "$region|$timestamp|$timestampSecondsInto2017|$timestampYear|$timestampMonth|$timestampDay|$timestampHour|$timestampMinute|$timestampSecond|$instanceId|$instanceType|$keyName|$state"
  }
  }
 }
}

def h = new Relational()
h.foo()
