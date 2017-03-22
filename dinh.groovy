package example

import groovy.json.*
import groovyx.net.http.*

import groovy.io.FileType

import java.sql.*

class Dinh {
 def jsonSlurper
 String inputCsvSuffix //02 for Amazon's February usage data csv
 String outputTableSuffix //e.g. "5" to create tables dinhraw$outputTableSuffix and dinh$outputTableSuffix

 Dinh() {
  jsonSlurper = new JsonSlurper()
 }

 void step3() {

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

  def stmt2 = conn.createStatement()
  try {
   String sql2 = "drop table cat.public.dinhraw$outputTableSuffix"
   def x2 = stmt2.execute(sql2)
  }
  catch (e) {
   println "no table cat.public.dinhraw$outputTableSuffix is available to drop"
  }

  /**
  * Use h2's CSVREAD
  */
  def stmt3 = conn.createStatement()
//String temp1 = 'invoiceID,payerAccountId,linkedAccountId,recordType,recordId,productName,rateId,subscriptionId,pricingPlanId,usageType,operation,availabilityZone,reservedInstance,itemDescription,usageStartDateRaw,usageEndDateRaw,usageQuantity,blendedRate,blendedCost,unblendedRate,unblendedCost,resourceId,ignore1,ignore2' 

  String sql3 = '' +
   "create table cat.public.dinhraw$outputTableSuffix (" +

' invoiceID varchar(100),' +
' payerAccountId varchar(100),' +
' linkedAccountId varchar(100),' +
' recordType varchar(100),' +
' recordId varchar(100),' +

' productName varchar(100),' +
' rateId varchar(100),' +
' subscriptionId varchar(100),' +
' pricingPlanId varchar(100),' +
' usageType varchar(100),' +

' operation varchar(100),' +
' availabilityZone varchar(100),' +
' reservedInstance varchar(100),' +
' itemDescription varchar(200),' +
' usageStartDateRaw varchar(100),' +

' usageEndDateRaw varchar(100),' +
' usageQuantity varchar(100),' + //decimal
' blendedRate varchar(100),' + //decimal
' blendedCost varchar(100),' + //decimal
' unblendedRate varchar(100),' + //decimal
' unblendedCost varchar(100),' + //decimal
' resourceId varchar(200),' +
' awsCreatedBy varchar(100),' +
' userProject varchar(100)) as ' +
" select * " +
"from CSVREAD('398274688464-aws-billing-detailed-line-items-with-resources-and-tags-2017-${inputCsvSuffix}.csv'); " +
//Yes, the csv does have a header.
//Here is the syntax to use if you're debugging and
// removing the header:
//"from CSVREAD('suspicious3', '$temp1', 'charset=UTF-8 fieldSeparator=,'); " +
"update cat.public.dinhraw$outputTableSuffix set usageQuantity = '0.0' where usageQuantity = ''; " +
"update cat.public.dinhraw$outputTableSuffix set blendedRate   = '0.0' where blendedRate = ''; " +
"update cat.public.dinhraw$outputTableSuffix set blendedCost   = '0.0' where blendedCost = ''; " +
"update cat.public.dinhraw$outputTableSuffix set unblendedRate = '0.0' where unblendedRate = ''; " +
"update cat.public.dinhraw$outputTableSuffix set unblendedCost = '0.0' where unblendedCost = ''; " +
"update cat.public.dinhraw$outputTableSuffix set userProject = replace(replace(replace(replace(replace(userProject, '-dev', ''), '-int', ''), '-stage', ''), '-test', ''), '-prod', ''); " +
"delete from cat.public.dinhraw$outputTableSuffix where usageStartDateRaw = ''; " 

  def x3 = stmt3.execute(sql3)

  def stmt5 = conn.createStatement()
  try {
   String sql5 = "drop table cat.public.dinh$outputTableSuffix"
   def x5 = stmt5.execute(sql5)
  }
  catch (e) {
   println "no table cat.public.dinh$outputTableSuffix is available to drop"
  }

  def stmt4 = conn.createStatement()
  String sql4 = '' +
   "create table cat.public.dinh$outputTableSuffix (" +
' invoiceID varchar(100),' +
' payerAccountId varchar(100),' +
' linkedAccountId varchar(100),' +
' recordType varchar(100),' +
' recordId varchar(100),' +
' productName varchar(100),' +
' rateId varchar(100),' +
' subscriptionId varchar(100),' +
' pricingPlanId varchar(100),' +
' usageType varchar(100),' +
' operation varchar(100),' +
' availabilityZone varchar(100),' +
' reservedInstance varchar(100),' +
' itemDescription varchar(200),' +
' usageStartDate varchar(100),' +
' usageEndDate varchar(100),' +
' usageQuantity decimal,' +
' blendedRate decimal,' +
' blendedCost decimal,' +
' unblendedRate decimal,' +
' unblendedCost decimal,' +
' resourceId varchar(200), ' +
' awsCreatedBy varchar(100), ' +
' userProject varchar(100)) ' +
'as ' +
'select ' +
'invoiceID,' +
'payerAccountId,' +
'linkedAccountId,' +
'recordType,' +
'recordId,' +
'productName,' +
'rateId,' +
'subscriptionId,' +
'pricingPlanId,' +
'usageType,' +
'operation,' +
'availabilityZone,' +
'reservedInstance,' +
'itemDescription,' +
"PARSEDATETIME(usageStartDateRaw, 'yyyy-MM-dd HH:mm:SS', 'en') usageStartDate," +
"PARSEDATETIME(usageEndDateRaw,   'yyyy-MM-dd HH:mm:SS', 'en') usageEndDate," +
'convert(usageQuantity, decimal) usageQuantity, ' +
'convert(blendedRate, decimal) blendedRate, ' +
'convert(blendedCost, decimal) blendedCost, ' +
'convert(unblendedRate, decimal) unblendedRate, ' +
'convert(unblendedCost, decimal) unblendedCost, ' +
'resourceId, ' +
'awsCreatedBy, ' +
'userProject ' +
"from cat.public.dinhraw$outputTableSuffix " +
"where linkedaccountid = '539674021708' "

  def x4 = stmt4.execute(sql4)

  conn.close();
 }

 //It's easier to call an external batch script
 // than it is to execute the individual batch commands
 // from within Groovy
 // (due to Groovy's weird list syntax to execute()).
 void step4() {
  def s0 = ['./dinhstep4'].execute().text
  println s0
 }

 void step5() {

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

  def stmt2 = conn.createStatement()
  try {
   String sql2 = "drop table cat.public.volumes"
   def x2 = stmt2.execute(sql2)
  }
  catch (e) {
   println "no table cat.public.volumes is available to drop"
  }

  /**
  * Use h2's CSVREAD
  */
  def stmt3 = conn.createStatement()
  String temp1 = 'volumeid,instanceid'

  //Youl should have run step4 to update describe-volumes-three-regions.csv 
  String sql3 = '' +
   'create table cat.public.volumes (' +
' volumeid varchar(100),' +
' instanceid varchar(100)) as ' + 
" select * " +
" from CSVREAD('describe-volumes-three-regions.csv', '$temp1', 'charset=UTF-8 fieldSeparator=,'); " 

  def x3 = stmt3.execute(sql3)

  def stmt4 = conn.createStatement()
  try {
   String sql4 = "drop table cat.public.instanceproject"
   def x4 = stmt4.execute(sql4)
  }
  catch (e) {
   println "no table cat.public.instanceproject is available to drop"
  }

  def stmt5 = conn.createStatement()

  String sql5 = '' +
'create table instanceproject as ( ' +
' select distinct resourceid, userproject ' +
" from dinh$outputTableSuffix d " +
" where resourceid like 'i-%' " +
" and userproject <> '') "

  def x5 = stmt5.execute(sql5)

  def stmt6 = conn.createStatement()
  try {
   String sql6 = "drop table cat.public.volumeproject"
   def x6 = stmt6.execute(sql6)
  }
  catch (e) {
   println "no table cat.public.volumeproject is available to drop"
  }

  def stmt7 = conn.createStatement()

  String sql7 = '' +
'create table volumeproject as ( ' +
' select v.volumeid, i.userproject ' +
' from volumes v ' +
' join instanceproject i ' +
' on v.instanceid = i.resourceid ' +
')'

  def x7 = stmt7.execute(sql7)

  def stmt8 = conn.createStatement()

  String sql8 = """
--Update volumes based on their association with a tagged instance
UPDATE dinh$outputTableSuffix d SET userproject=(SELECT L.userproject FROM volumeproject L WHERE L.volumeid=d.resourceid) WHERE resourceId like 'vol-%';

--Update instances based on their later (chronologically) tagging
UPDATE dinh$outputTableSuffix d SET userproject=(SELECT L.userproject FROM instanceproject L WHERE L.resourceid=d.resourceid) WHERE resourceId like 'i-%' and userproject = '';

--The previous two UPDATE statements set many rows to null, 
-- so change them back to ‘’
UPDATE dinh$outputTableSuffix set userproject = '' where userproject is null;
"""
  def x8 = stmt8.execute(sql8)

  conn.close();
 }

}

def h = new Dinh()

//step3 uploads the csv file into a raw table and into a scrubbed table
if ((args[0]) == 'step3') {
 h.inputCsvSuffix = args[1]
 h.outputTableSuffix = args[2]
 h.step3()
 //step3 writes to h2
}

//step4 is a bash script called dinhstep4.
// It associates volumes with instances.
// It should be run before step5.
if ((args[0]) == 'step4') {
 h.step4()
}

//step5 loads helper tables and then updates the scrubbed table with add'l tags
if ((args[0]) == 'step5') {
 //step5 ignores args[1]
 h.outputTableSuffix = args[2]
 h.step5()
 //step5 writes to h2
}
