package example

import groovy.json.*
import groovyx.net.http.*

import groovy.io.FileType

class Hello {

 void capture() {
  def s = "aws ec2 describe-instances --region us-west-2".execute().text
  def timestamp = 'date +"%Y_%m_%d_%H_%M_%S"'.execute().text
   .replaceAll('"','')
   .replaceAll('\n','')

  def f = new File("captured/${timestamp}.json")
  f.write(s)
 }

 void analyze() {
  def files = []

  def dir = new File("captured")
  dir.eachFileRecurse (FileType.FILES) { file ->
   files << file
  }
  def sortedfiles = files.sort()
  //println sortedfiles

  sortedfiles.each { s ->
   def f = new File(s.toString())
   def t = ''
   f.eachLine {
    t += it
   }
   println t.size()
  }

  
 }
}

def h = new Hello()
h.analyze()
