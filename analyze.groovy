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
'd2.8xlarge':   5.52,
'g2.2xlarge':   0.65,
'g2.8xlarge':   2.6,
'i3.xlarge':    0.31,
'm3.medium':    0.11,
'm3.large':     0.13,
'm3.xlarge':    0.27,
'm3.2xlarge':   0.53,
'm4.large':     0.11,
'm4.xlarge':    0.21,
'm4.2xlarge':   0.43, //week=0.43*24*7=72
'm4.4xlarge':   0.86,
'r3.2xlarge':   0.66,
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
   for (def j = 0; j < object.Reservations[i].Instances.size(); j++) {
    def instanceId   = object.Reservations[i].Instances[j].InstanceId
    def instanceType = object.Reservations[i].Instances[j].InstanceType
    def keyName      = object.Reservations[i].Instances[j].KeyName
    def state        = object.Reservations[i].Instances[j].State.Name
    def projectValue = getTagValue(object.Reservations[i].Instances[j].Tags, 'Project')
    def nameValue    = getTagValue(object.Reservations[i].Instances[j].Tags, 'Name')

    print   "${f.region},${f.timestamp},${f.timestampSecondsInto2017},"
    print   "${f.timestampYear},${f.timestampMonth},${f.timestampDay},"
    print   "${f.timestampHour},${f.timestampMinute},${f.timestampSecond},"
    println "$instanceId,$instanceType,$keyName,$state,$projectValue,$nameValue"
   }
   }
 }

 void interpretEC2DescribeSecurityGroupsAndPrint(String region, t) {
  def object = jsonSlurper.parseText(t)
   for (def i = 0; i < object.SecurityGroups.size(); i++) {
    def groupId = object.SecurityGroups[i].GroupId
    def groupName = object.SecurityGroups[i].GroupName
    def description = object.SecurityGroups[i].Description
    description = description.replaceAll(',', ' comma')
    def ipPermissions = object.SecurityGroups[i].IpPermissions
    for (def j = 0; j < object.SecurityGroups[i].IpPermissions.size(); j++) {
     def fromPort = object.SecurityGroups[i].IpPermissions[j].FromPort
     def ipRanges = object.SecurityGroups[i].IpPermissions[j].IpRanges
     for (def k = 0; k < ipRanges.size(); k++) {
      def cidrIp = ipRanges[k].CidrIp
      def shortDesc = (description.size() >= 12)? description[0..11]: description
      println "$region,$groupId,$groupName,$description,$fromPort,$cidrIp"
     }
    }
   }
 }

 void analyzeOfficeMoveToHerndonStep1_FlattenDescribeInstances(String regionWithDashes) {
  ArrayList sortedfiles = getNamesOfCapturedFiles("captured/$regionWithDashes")
  String region
  String inputfilename = sortedfiles[-1].toString()
  if (inputfilename =~ /east-1/) region = 'us-east-1'
  if (inputfilename =~ /west-1/) region = 'us-west-1'
  if (inputfilename =~ /west-2/) region = 'us-west-2'
  String t = new File(inputfilename).text
  interpretEC2jsonForTrueSecurityGroupsAndPrint(region, t)
 }

 void interpretEC2jsonForTrueSecurityGroupsAndPrint(region, t) {
   //The interpreted json values are printed here as a convenience
   // rather than returning them within an array.
   def object = jsonSlurper.parseText(t)
   for (def i = 0; i < object.Reservations.size(); i++) {
    for (def j = 0; j < object.Reservations[i].Instances.size(); j++) {
     def instanceId     = object.Reservations[i].Instances[j].InstanceId
     def securityGroups = object.Reservations[i].Instances[j].SecurityGroups
     for (def k = 0; k < securityGroups.size(); k++) {
      def groupName = securityGroups[k].GroupName
      def groupId   = securityGroups[k].GroupId
      println "$region,$instanceId,$groupId,$groupName"
     }
    }
   }
 }

 void analyzeOfficeMoveToHerndonStep1a_Upload(inputfilename) {
  Class.forName("org.h2.Driver");
  Connection conn = DriverManager.
  getConnection("jdbc:h2:tcp://localhost/~/blueorangeh2/cat", "sa", "");

  def stmt2 = conn.createStatement()
  try {
   String sql2 = "drop table cat.public.herndon_instancesecgrp"
   def x2 = stmt2.execute(sql2)
  }
  catch (e) {
  }

  def stmt3 = conn.createStatement()
  String sql3 = '' +
   'create table cat.public.herndon_instancesecgrp (' +
   ' region varchar(100),' +
   ' instanceId varchar(100),' + 
   ' groupId varchar(100),' +
   ' groupName varchar(100))' +
   " as select * from CSVREAD('deleteme4')"
   .replaceAll('deleteme4',inputfilename)

   def x3 = stmt3.execute(sql3)
 }

 void analyzeOfficeMoveToHerndonStep2_FlattenDescribeSecurityGroups(String inputfilename) {
   String region
   if (inputfilename =~ /east1/) region = 'us-east-1'
   if (inputfilename =~ /west1/) region = 'us-west-1'
   if (inputfilename =~ /west2/) region = 'us-west-2'
   String t = new File(inputfilename).text
   interpretEC2DescribeSecurityGroupsAndPrint(region, t)
 }

 void analyzeOfficeMoveToHerndonStep3_Upload(inputfilename) {
  Class.forName("org.h2.Driver");
  Connection conn = DriverManager.
  getConnection("jdbc:h2:tcp://localhost/~/blueorangeh2/cat", "sa", "");

  def stmt2 = conn.createStatement()
  try {
   String sql2 = "drop table cat.public.herndon_207"
   def x2 = stmt2.execute(sql2)
  }
  catch (e) {
  }

  def stmt3 = conn.createStatement()
  String sql3 = '' +
   'create table cat.public.herndon_207 (' +
   ' region varchar(100),' +
   ' groupId varchar(100),' + 
   ' groupName varchar(100),' +
   ' description varchar(200),' +
   ' fromPort varchar(100),' +
   ' cidrIp varchar(100))' +
   " as select * from CSVREAD('deleteme4')"
   .replaceAll('deleteme4',inputfilename)

   def x3 = stmt3.execute(sql3)
 }

 void analyzeOfficeMoveToHerndonStep1_FlattenDescribeNetworkAcls(String inputfilename) {
  String region
  if (inputfilename =~ /east1/) region = 'us-east-1'
  if (inputfilename =~ /west1/) region = 'us-west-1'
  if (inputfilename =~ /west2/) region = 'us-west-2'
  String t = new File(inputfilename).text
  interpretEC2jsonForNetworkAclsAndPrint(region, t)
 }

 void interpretEC2jsonForNetworkAclsAndPrint(region, t) {
   def object = jsonSlurper.parseText(t)
//xxxxxx
   for (def i = 0; i < object.NetworkAcls.size(); i++) {
    def networkAclId = object.NetworkAcls[i].NetworkAclId
    def entries = object.NetworkAcls[i].Entries
    for (def j = 0; j < object.NetworkAcls[i].Entries.size(); j++) {
     def cidrBlock = object.NetworkAcls[i].Entries[j].CidrBlock
     println "$region,$networkAclId,$cidrBlock"
/*
     def instanceId     = object.Reservations[i].Instances[j].InstanceId
     def securityGroups = object.Reservations[i].Instances[j].SecurityGroups
     for (def k = 0; k < securityGroups.size(); k++) {
      def groupName = securityGroups[k].GroupName
      def groupId   = securityGroups[k].GroupId
      println "$region,$instanceId,$groupId,$groupName"
     }
*/
    }
   }
 }
 void step1() {
  def sortedfiles = getNamesOfCapturedFiles('captured')

  sortedfiles.each { s ->
   HashMap f = interpretNameOfCapturedFile('captured', s)
   String t = new File(s.toString()).text
   try {
    interpretEC2jsonAndPrint(f, t)
   }
   catch (e) {
    //Sometimes "aws ec2 describe-instances" produces an empty file.
    // Just ignore the file and its related Exception,
    // "Text must not be null or empty."
   }
  }
 }

 String getTagValue(tags, tag) {
  def tagvalue = "no${tag}tag"

  if (tags) {
   for (int i=0; i<tags.size(); i++) {
    if (tags[i].Key == tag) {
     tagvalue = tags[i].Value
    }
   }
  }

  tagvalue
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
  //getConnection("jdbc:h2:~/cat", "sa", "");
  getConnection("jdbc:h2:tcp://localhost/~/blueorangeh2/cat", "sa", "");

  def stmt2 = conn.createStatement()
  try {
   String sql2 = "drop table cat.public.blue_step1"
   def x2 = stmt2.execute(sql2)
  }
  catch (e) {
  }

  def stmt3 = conn.createStatement()
  String sql3 = '' +
   'create table cat.public.blue_step1 (' +
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
   ' projectValue varchar(100),' +
   ' nameValue varchar(100))' +
   " as select * from CSVREAD('deleteme4')"
   .replaceAll('deleteme4',inputfilename)

   def x3 = stmt3.execute(sql3)

  def stmt4 = conn.createStatement();
  String sql4 = "select * from blue_step1 order by instanceid, xtimestamp"
  ResultSet rs4 = stmt4.executeQuery(sql4);

  print   "region,instanceid,xtimestamp,savepreviousxtimestamp,"
  print   "xtimestampSecondsInto2017,xyear,xmonth,xday,xhour,xminute,xsecond,"
  print   "xsavepreviousxtimestampSecondsInto2017,secondsPassed,type,"
  println "secgrp,status,projectValue,nameValue,costperthisline"

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
   String nameValue    = rs4.getString("nameValue");

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
   if (!costperhour) {
      System.err << "Unknown instance type: $type\n"
      costperhour = 0.01
   }
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
   println "$secgrp,$status,$projectValue,$nameValue,$costperthisline"

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
  getConnection("jdbc:h2:tcp://localhost/~/blueorangeh2/cat", "sa", "");
  //getConnection("jdbc:h2:~/cat", "sa", "");

  /**
  * Only one table is used for ad hoc analysis
  */
  def stmt2 = conn.createStatement()
  try {
   String sql2 = "drop table cat.public.blue_step3"
   def x2 = stmt2.execute(sql2)
  }
  catch (e) {
   println "no table cat.public.blue_step3 is available to drop"
  }

  /**
  * Use h2's CSVREAD
  */
  def stmt3 = conn.createStatement()
  String sql3 = '' +
   'create table cat.public.blue_step3 (' +
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
   ' nameValue varchar(100),' +
   ' costperthisline decimal)' +
   " as select * from CSVREAD('deleteme4'); ".replaceAll('deleteme4',inputfilename) +
   "update cat.public.blue_step3 set projectValue = replace(replace(replace(replace(replace(projectValue, '-dev', ''), '-int', ''), '-stage', ''), '-test', ''), '-prod', ''); " 

   def x3 = stmt3.execute(sql3)
println "I just executed this:\n$sql3"
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

if ((args[0]) == 'analyzeOfficeMoveToHerndonStep1_FlattenDescribeInstances') {
 String regionWithDashes= args[1]
 h.analyzeOfficeMoveToHerndonStep1_FlattenDescribeInstances(regionWithDashes)
}
if ((args[0]) == 'analyzeOfficeMoveToHerndonStep1a_Upload') {
 def inputfilename = args[1]
 h.analyzeOfficeMoveToHerndonStep1a_Upload(inputfilename)
}
if ((args[0]) == 'analyzeOfficeMoveToHerndonStep2_FlattenDescribeSecurityGroups') {
 def inputfilename = args[1]
 h.analyzeOfficeMoveToHerndonStep2_FlattenDescribeSecurityGroups(inputfilename)
}
if ((args[0]) == 'analyzeOfficeMoveToHerndonStep3_Upload') {
 def inputfilename = args[1]
 h.analyzeOfficeMoveToHerndonStep3_Upload(inputfilename)
}
if ((args[0]) == 'analyzeOfficeMoveToHerndonStep1_FlattenDescribeNetworkAcls') {
 String inputfilename = args[1]
 h.analyzeOfficeMoveToHerndonStep1_FlattenDescribeNetworkAcls(inputfilename)
}

