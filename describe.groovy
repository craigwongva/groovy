package example

import groovy.json.*
import groovyx.net.http.*

class Hello {

 void capture() {
  def s = "aws ec2 describe-instances --region us-west-2".execute().text
  def timestamp = 'date +"%Y_%m_%d_%H_%M_%S"'.execute().text
   .replaceAll('"','')
   .replaceAll('\n','')

  def f = new File("captured/${timestamp}.json")
  f.write(s)
 }
}

def h = new Hello()
h.capture()
