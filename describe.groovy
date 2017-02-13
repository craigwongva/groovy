package example

import groovy.json.*
import groovyx.net.http.*

class Hello {

 void capture(String region) {
  def s = "aws ec2 describe-instances --region $region".execute().text
  def timestamp = 'date +"%Y_%m_%d_%H_%M_%S"'.execute().text
   .replaceAll('"','')
   .replaceAll('\n','')

  def f = new File("captured/$region/${timestamp}.json")
  f.write(s)
 }
}

def h = new Hello()
while (1 > 0) {
 h.capture('us-west-2')
 h.capture('us-east-1')
 sleep 1000*(5*60)
}
