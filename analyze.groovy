package example

import groovy.json.*
import groovyx.net.http.*

import groovy.io.FileType

class Hello {

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

  println "I'm getting file list from /captured"
  def dir = new File("captured")
  dir.eachFileRecurse (FileType.FILES) { file ->
   files << file
  }
  def sortedfiles = files.sort() //[files[0]] //s/m: undo this comment: files.sort()
  //println sortedfiles

  println "I'm building hash table of form [filename:json]"
  def snapshot = [:]
  sortedfiles.each { s ->
   //println s
/*
   def f = new File(s.toString())
   def t = ''
   def line = 0
   f.eachLine {
    if (line++ % 1000 == 0) print '.'
    t += it
   }
   println ""
*/
   def t = new File(s.toString()).text
   snapshot[s.toString()] = t
   //println s.toString()
  }

  println "I'm visiting each json:"
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

def h = new Hello()
h.analyze()
