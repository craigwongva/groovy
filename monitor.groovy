import groovy.io.FileType

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

def files = getNamesOfCapturedFiles('captured')
println "HTTP/1.0 200 OK\n"
def lastfilename = files[files.size()-1]
int lastfiletime = interpretNameOfCapturedFile('captured', lastfilename)
 .timestampSecondsInto2017
println "Last file name is $lastfilename"

def nowtimestamp = 'date +"%Y_%m_%d_%H_%M_%S"'.execute().text
 .replaceAll('"','')
 .replaceAll('\n','')
def nowfilename = "captured/us-east-1/${nowtimestamp}.json"
int nowfiletime = interpretNameOfCapturedFile('captured', nowfilename)
 .timestampSecondsInto2017
println "Now is . . . . . . . . . . . . . . . $nowtimestamp"
int timediff = nowfiletime - lastfiletime
println "Elapsed time is ${(timediff/60).toInteger()} minutes ${timediff % 60} seconds"
if (timediff < 36*60) {
 println "status green"
}
else {
 println "status red"
}

return "success"
/*

 void capture(String region) {
  def s = "aws ec2 describe-instances --region $region".execute().text
  def timestamp = 'date +"%Y_%m_%d_%H_%M_%S"'.execute().text
   .replaceAll('"','')
   .replaceAll('\n','')

  def f = new File("captured/$region/${timestamp}.json")
  f.write(s)
 }
*/
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
