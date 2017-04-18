package example

import groovy.json.*
import groovyx.net.http.*

import groovy.io.FileType

import java.sql.*

class Orange {
//xxx def jsonSlurper
 String inputCsvSuffix //02 for Amazon's February usage data csv
 String outputTableSuffix //e.g. "5" to create tables dinhraw$outputTableSuffix and dinh$outputTableSuffix

 void step3() {

  //blueorange refers to:
  // blue: homegrown code that captures describe-instance data,
  // orange: AWS-provided billing data
  //cat is an arbitrary name
  Class.forName("org.h2.Driver");
  Connection conn = DriverManager.
  // Use the default username/password and store it publicly because:
  //  - the data is to be considered private but not sensitive
  //  - the h2 console is restricted by IP
   getConnection("jdbc:h2:tcp://localhost/~/blueorangeh2/cat", "sa", "");

  //dinh refers to:
  // the AWS-provided billing data that Dinh analyzed in Excel
  //Therefore orange_dinh is a redundant name
  //The outputTableSuffix value is arbitrary.
  // Recently I have been using '4', then '5', then '6'.
  def stmt1 = conn.createStatement()
  try {
   String sql1 = "drop table cat.public.orange_dinhraw$outputTableSuffix"
   stmt1.execute(sql1)
  }
  catch (e) {
   println "no table cat.public.orange_dinhraw$outputTableSuffix is available to drop"
  }

  /**
  * Use h2's CSVREAD to ingest AWS billing details
  */
  def stmt2 = conn.createStatement()

  String sql2 = '' +
"create table cat.public.orange_dinhraw$outputTableSuffix (" +

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
" from CSVREAD('398274688464-aws-billing-detailed-line-items-with-resources-and-tags-2017-${inputCsvSuffix}.csv'); " +
"update cat.public.orange_dinhraw$outputTableSuffix set usageQuantity = '0.0' where usageQuantity = ''; " +
"update cat.public.orange_dinhraw$outputTableSuffix set blendedRate   = '0.0' where blendedRate = ''; " +
"update cat.public.orange_dinhraw$outputTableSuffix set blendedCost   = '0.0' where blendedCost = ''; " +
"update cat.public.orange_dinhraw$outputTableSuffix set unblendedRate = '0.0' where unblendedRate = ''; " +
"update cat.public.orange_dinhraw$outputTableSuffix set unblendedCost = '0.0' where unblendedCost = ''; " +
"update cat.public.orange_dinhraw$outputTableSuffix set userProject = " +
" replace(replace(replace(replace(replace(userProject, '-dev', ''), '-int', ''), '-stage', ''), '-test', ''), '-prod', ''); " +
"delete from cat.public.orange_dinhraw$outputTableSuffix where usageStartDateRaw = ''; " 

  try {
   stmt2.execute(sql2)
  }
  catch (e) {
   println e
  }

  def stmt3 = conn.createStatement()
  try {
   String sql3 = "drop table cat.public.orange_dinh$outputTableSuffix"
   stmt3.execute(sql3)
  }
  catch (e) {
   println "no table cat.public.orange_dinh$outputTableSuffix is available to drop"
  }

  /**
  * Transform the raw data, e.g.
  * 1. convert to numbers where appropriate
  * 2. filter where account is 1708
  */
  def stmt4 = conn.createStatement()
  String sql4 = '' +
   "create table cat.public.orange_dinh$outputTableSuffix (" +
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
"from cat.public.orange_dinhraw$outputTableSuffix " +
"where linkedaccountid = '539674021708' "

  try {
   stmt4.execute(sql4)
  }
  catch (e) {
   println e
  }

  conn.close();
 }

 //It's easier to call an external batch script
 // than it is to execute the individual batch commands
 // from within Groovy
 // (due to Groovy's weird list syntax to execute()).
 void step4() {
  def s0 = ['./orangestep4'].execute().text
  println s0
 }

 void step5() {

  Class.forName("org.h2.Driver");
  Connection conn = DriverManager.
  getConnection("jdbc:h2:tcp://localhost/~/blueorangeh2/cat", "sa", "");

  def stmt1 = conn.createStatement()
  try {
   String sql1 = "drop table cat.public.orange_volumes"
   stmt1.execute(sql1)
  }
  catch (e) {
   println "no table cat.public.orange_volumes is available to drop"
  }

  /**
  * Use h2's CSVREAD to ingest volumes data.
  */
  def stmt2 = conn.createStatement()

  //You should have already run step4 to update describe-volumes-three-regions.csv 
  String sql2 = '' +
   'create table cat.public.orange_volumes (' +
' volumeid varchar(100),' +
' instanceid varchar(100)) as ' + 
" select * " +
" from CSVREAD('describe-volumes-three-regions.csv', 'volumeid,instanceid', 'charset=UTF-8 fieldSeparator=,'); " 
  try {
   stmt2.execute(sql2)
  }
  catch (e) {
   println e
  }

  def stmt3 = conn.createStatement()
  try {
   String sql3 = "drop table cat.public.orange_instanceproject"
   stmt3.execute(sql3)
  }
  catch (e) {
   println "no table cat.public.orange_instanceproject is available to drop"
  }

  def stmt4 = conn.createStatement()

  String sql4 = '' +
'create table cat.public.orange_instanceproject as ( ' +
' select distinct resourceid, userproject ' +
" from cat.public.orange_dinh$outputTableSuffix d " +
" where resourceid like 'i-%' " +
" and userproject <> '') "

  try {
   stmt4.execute(sql4)
  }
  catch (e) {
   println e
  }

  def stmt6 = conn.createStatement()
  try {
   String sql6 = "drop table cat.public.orange_volumeproject"
   stmt6.execute(sql6)
  }
  catch (e) {
   println "no table cat.public.orange_volumeproject is available to drop"
  }

  def stmt7 = conn.createStatement()

  String sql7 = '' +
'create table cat.public.orange_volumeproject as ( ' +
' select v.volumeid, i.userproject ' +
' from cat.public.orange_volumes v ' +
' join cat.public.orange_instanceproject i ' +
' on v.instanceid = i.resourceid ' +
')'

  try {
   stmt7.execute(sql7)
  }
  catch (e) {
   println e
  } 

  def stmt8 = conn.createStatement()

  String sql8 = """
--Update volumes based on their association with a tagged instance
UPDATE cat.public.orange_dinh$outputTableSuffix d SET userproject=(SELECT L.userproject FROM cat.public.orange_volumeproject L WHERE L.volumeid=d.resourceid) WHERE resourceId like 'vol-%';

--Update instances based on their later (chronologically) tagging
UPDATE cat.public.orange_dinh$outputTableSuffix d SET userproject=(SELECT L.userproject FROM cat.public.orange_instanceproject L WHERE L.resourceid=d.resourceid) WHERE resourceId like 'i-%' and userproject = '';

--The previous two UPDATE statements set many rows to null, 
-- so change them back to ‘’
UPDATE cat.public.orange_dinh$outputTableSuffix set userproject = '' where userproject is null;
"""
  try {
   stmt8.execute(sql8)
  }
  catch (e) {
   println e
  } 

  conn.close();
 }

}

def h = new Orange()

//step3 uploads the csv file into a raw table and into a scrubbed table
if ((args[0]) == 'step3') {
 h.inputCsvSuffix = args[1]
 h.outputTableSuffix = args[2]
 h.step3()
 //step3 writes to h2
}

//step4 is a bash script called orangestep4.
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
//The following statement executes the method 
// passed in as the args[0] parameter.
//new Orange().{args[0]}(args)
