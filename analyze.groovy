package example

import groovy.json.*
import groovyx.net.http.*

import groovy.io.FileType

class Hello {

 void analyze() {
  def files = []

  def dir = new File("captured")
  dir.eachFileRecurse (FileType.FILES) { file ->
   files << file
  }
  def sortedfiles = files.sort()

  sortedfiles.each { s ->
   def t = new File(s.toString()).text
   def sskey = s.toString()
   def region = sskey['captured/'.size()..'captured/us-east-1'.size()-1] 
   def timestamp = sskey[
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

   def jsonSlurper = new JsonSlurper()
   def object = jsonSlurper.parseText(t) 
   for (def i = 0; i < object.Reservations.size(); i++) {
    def instanceId   = object.Reservations[i].Instances[0].InstanceId
    def instanceType = object.Reservations[i].Instances[0].InstanceType
    def keyName      = object.Reservations[i].Instances[0].KeyName
    def state        = object.Reservations[i].Instances[0].State.Name
    def projectValue = getTagProjectValue(object.Reservations[i].Instances[0].Tags)

    print   "$region,$timestamp,$timestampSecondsInto2017,"
    print   "$timestampYear,$timestampMonth,$timestampDay,"
    print   "$timestampHour,$timestampMinute,$timestampSecond,"
    println "$instanceId,$instanceType,$keyName,$state,$projectValue"
   }
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

}

def h = new Hello()
h.analyze()
