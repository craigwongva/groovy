package example

import groovy.json.*
import groovyx.net.http.*

import groovy.io.FileType

import java.sql.*

class Hello {
 Connection conn
 def jsonSlurper

 Hello() {
  Class.forName("org.h2.Driver");
  conn = DriverManager.
   getConnection("jdbc:h2:tcp://localhost/~/univision", "sa", "");
   //getConnection("jdbc:h2:~/univision", "sa", "");

  jsonSlurper = new JsonSlurper()
 }

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
    def instanceId   = object.Reservations[i].Instances[0].InstanceId
    def instanceType = object.Reservations[i].Instances[0].InstanceType
    def keyName      = object.Reservations[i].Instances[0].KeyName
    def state        = object.Reservations[i].Instances[0].State.Name
    def projectValue = getTagValue(object.Reservations[i].Instances[0].Tags, 'Project')
    def nameValue    = getTagValue(object.Reservations[i].Instances[0].Tags, 'Name')

    print   "${f.region},${f.timestamp},${f.timestampSecondsInto2017},"
    print   "${f.timestampYear},${f.timestampMonth},${f.timestampDay},"
    print   "${f.timestampHour},${f.timestampMinute},${f.timestampSecond},"
    println "$instanceId,$instanceType,$keyName,$state,$projectValue,$nameValue"
   }
 }

 void step1() {
  def sortedfiles = getNamesOfCapturedFiles('captured')

  sortedfiles.each { s ->
   HashMap f = interpretNameOfCapturedFile('captured', s)
   String t = new File(s.toString()).text
   interpretEC2jsonAndPrint(f, t)
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
  getConnection("jdbc:h2:tcp://localhost/~/cat", "sa", "");

  def stmt2 = conn.createStatement()
  try {
   String sql2 = "drop table cat.public.step1"
   def x2 = stmt2.execute(sql2)
  }
  catch (e) {
  }

  def stmt3 = conn.createStatement()
  String sql3 = '' +
   'create table cat.public.step1 (' +
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
  String sql4 = "select * from step1 order by instanceid, xtimestamp"
  ResultSet rs4 = stmt4.executeQuery(sql4);

  def i=0
  while(rs4.next() && i++ <= 2000000) {
   String region       = rs4.getString("region");

   print   "$region,$instanceid,$xtimestamp,$savepreviousxtimestamp,"
  }

  conn.close();
 }

 void step11(sentence) {
  String sql1 = "insert into sentences(s) values('$sentence')"
  def stmt1 = conn.prepareStatement(sql1, Statement.RETURN_GENERATED_KEYS)
  def x1 = stmt1.execute()
  ResultSet rs1 = stmt1.getGeneratedKeys()
  rs1.next()
  def rs1key = rs1.getInt(1) 

  String sql2 = convertWordListToInsertStatements(rs1key, sentence)
  println sql2
  def stmt2 = conn.createStatement()
  try {
   def x2 = stmt2.execute(sql2)
  }
  catch (e) {
   println "insert failed"
   println e
  }
 }

 String convertWordListToInsertStatements(rs1key, String s) {
  String t = ''
  s.split(' ').each {
   t += "insert into labels(sid, l) values($rs1key, 'has-word-$it');"
  }
  t
 }

 String getWordInHyphenedLabel(hyphenedLabel) {
  hyphenedLabel[-hyphenedLabel.reverse().indexOf('-')..-1]
 }
/*
--Return one row. Not an optimized query!
select * from (
select rownum r, s.*, l.id lid, l.sid, l.l
from sentences s
join labels l
on s.id = l.sid
where s.id <> 3
--and s.s regexp 'faltar'
and l in (
select l.l
from labels l
where sid = 3
)
)
order by rand()
limit 1
*/
 void step17oneline(int n, String regexp) {
  def stmt4 = conn.createStatement();
  String sql4 = ''
   //Find sentences matching a certain sentence
   //Return one row. Not an optimized query!
   sql4 += 'select * from ( '
   sql4 += 'select rownum r, s.id ids, s.s, l.id idl, l.sid, l.l '
   sql4 += 'from sentences s '
   sql4 += 'join labels l '
   sql4 += 'on s.id = l.sid '
   sql4 += "where s.id <> $n "
   sql4 += "and s.s regexp '$regexp' "
   sql4 += 'and l in ( '
   sql4 += 'select l.l '
   sql4 += 'from labels l '
   sql4 += "where sid = $n "
   sql4 += ') '
   sql4 += ') '
   sql4 += 'order by rand() '
   sql4 += 'limit 1 '

  ResultSet rs4 = stmt4.executeQuery(sql4);

  def i=0
  while(rs4.next() && i++ <= 2000000) {
   String r   = rs4.getString("r");
   String ids = rs4.getString("ids");
   String s   = rs4.getString("s");
   String idl = rs4.getString("idl");
   String sid = rs4.getString("sid");
   String l   = rs4.getString("l");

   println "$i $r,$ids,$s,$idl,$sid,$l"
  }
 }

 void step17(int n, String regexp) {
  def stmt4 = conn.createStatement();
  String sql4 = ''
   //Find sentences matching a certain sentence
   sql4 += 'select s.id ids, s.s, l.id idl, l.sid, l.l '
   sql4 += 'from sentences s '
   sql4 += 'join labels l '
   sql4 += 'on s.id = l.sid '
   sql4 += "where s.id <> $n "
   sql4 += "and s.s regexp '$regexp' "
   sql4 += 'and l in ( '
   sql4 += 'select l.l '
   sql4 += 'from labels l '
   sql4 += "where sid = $n "
   sql4 += ') '

  ResultSet rs4 = stmt4.executeQuery(sql4);

  def i=0
  while(rs4.next() && i++ <= 2000000) {
   String ids = rs4.getString("ids");
   String s   = rs4.getString("s");
   String idl = rs4.getString("idl");
   String sid = rs4.getString("sid");
   String l   = rs4.getString("l");

   println "$ids,$s,$idl,$sid,$l"
  }
 }

 void step16(int n) {
  def stmt4 = conn.createStatement();
  String sql4 = ''
   //Find sentences matching a certain sentence
   sql4 += 'select s.id ids, s.s, l.id idl, l.sid, l.l '
   sql4 += 'from sentences s '
   sql4 += 'join labels l '
   sql4 += 'on s.id = l.sid '
   sql4 += "where s.id <> $n "
//and s.s regexp 'faltar'
   sql4 += 'and l in ( '
   sql4 += 'select l.l '
   sql4 += 'from labels l '
   sql4 += "where sid = $n "
   sql4 += ') '

  ResultSet rs4 = stmt4.executeQuery(sql4);

  def i=0
  while(rs4.next() && i++ <= 2000000) {
   String ids = rs4.getString("ids");
   String s   = rs4.getString("s");
   String idl = rs4.getString("idl");
   String sid = rs4.getString("sid");
   String l   = rs4.getString("l");

   println "$ids,$s,$idl,$sid,$l"
  }
 }

 void step15(int n) {
  def stmt4 = conn.createStatement();
  String sql4 = ''
   //Find labels in a certain sentence
   sql4 += 'select l.l '
   sql4 += 'from labels l '
   sql4 += "where sid = $n "

  ResultSet rs4 = stmt4.executeQuery(sql4);

  def i=0
  while(rs4.next() && i++ <= 2000000) {
   String l = rs4.getString("l");

   println "$l"
  }
 }

 void step14(String hyphenedLabel, ArrayList words) {
  String wordInHyphenedLabel =
   getWordInHyphenedLabel(hyphenedLabel)

  def stmt4 = conn.createStatement();
  String sql4 = ''

  sql4 += "delete from labels where l = '$hyphenedLabel'; "
  sql4 += 'insert into labels (sid, l) '
  sql4 += "select id, '$hyphenedLabel' "
  sql4 += 'from sentences s '
  sql4 += "where s.s regexp '$wordInHyphenedLabel' "
  words.each {
   sql4 += "or s.s regexp '$it' "
  }
  println sql4
  stmt4.execute(sql4);
 }

 void step13(hyphenedLabel) {
  String wordInHyphenedLabel =
   getWordInHyphenedLabel(hyphenedLabel)

  def stmt4 = conn.createStatement();
  String sql4 = ''

  sql4 += "delete from labels where l = '$hyphenedLabel'; "
  sql4 += 'insert into labels (sid, l) '
  sql4 += "select id, '$hyphenedLabel' "
  sql4 += 'from sentences s '
  sql4 += "where s.s regexp '$wordInHyphenedLabel' "

  stmt4.execute(sql4);
 }

 void step12(String hyphenedLabel) {
  def stmt4 = conn.createStatement();
  String sql4 = ''
   //Find sentences with a certain label
   sql4 += 'select s.* '
   sql4 += 'from sentences s '
   sql4 += 'join labels l '
   sql4 += 'on s.id = l.sid '
   sql4 += "where l = '$hyphenedLabel' "

  ResultSet rs4 = stmt4.executeQuery(sql4);

  def i=0
  while(rs4.next() && i++ <= 2000000) {
   String s = rs4.getString("s");

   println "$s"
  }
 }

}

def h = new Hello()
if ((args[0]) == 'step11') {
 def temp = [
 'Siempre hay una manera positiva de ver las cosas, buscala',
 'Domingo para reflexionar en las bendiciones que se te han dado',
 'No puedo parar de mirarlo',
 'Ahora conversan y opinan si les gustan mas los amores inocentes or con experiencia',
 'Si quieres cenar algo ligera. Intenta esta Ensalada Griega',
 'Este comercial te hara querer abrazar a tu perrito',
 'Felizmente me quedo con esta vista, con esta paz y tranquilidad que solo ofrece Castel Gandolfo',
 'En tu closet no puede faltar un vestido negro',
 'No deseches tus frascos',
 'Mira como decorarlos para que sirvan de adornos en tu hogar',
 'Aprende a decorar tus frascos ahora y reutilizalos',
 'Tu pareja gasta mas dinero que tu?',
 'Aprende como evitar problemas',
 'Este miercoles te damos los trucos para lucir bien elegante en shorts',
 'Manana voy a estar contestando sus preguntas sobre moda y estilo, a tiempo para el verano',
 'Envien sus preguntas usando #AskThalia',
 'Hoy es su cumpleanos',
 'Hoy cumpleanos la periodista hondurena y copresentadora de @despiertamerica',
 'Todos abrazaron a @satchapretto en un dia muy especial',
 'Si te preguntabas que es #PanamaPapers, aqui te compartimos un resumen',
 'Feliz inicio de semana',
 'Conoce ahora a la hija de @CristianCastro',
 'Tiene dos anitos y es una estrella de las redes sociales',
 'Si quieres que tus hijos tengan una vida adulta emocional saludable',
 'Mira que puedes hacer',
 'Enterate como hacer que la cama de tu mascota sea parte integral del diseno de casa',
 'Que felicidad regresar a mi universidad',
 'Por que Playboy esta apunto de morir',
 'Nosotros tambien felices de tenerte con nosotros',
 'Es viernes y mi cuerpo lo sabe',
 'Gracias por ver @despiertamerica hoy Lunes',
 'Recuerden que pueden ver el show si te lo perdiste en la aplicacion #UnivisionNow',
 'Alguna vez has espiado a tu pareja',
 'Manana la @dranancyalvarez te dice como acabar con esta mania',
 ]
 println "Executing step11 now (insert sample sentences and insert has-word labels)"
 int i = 0
 temp.each {
  if (i++ <= 1000) {
   h.step11(it)
  }
 }
}

if ((args[0]) == 'step12') {
 println "Executing step12 (select labels like has-root-poder)"
 h.step12('has-root-poder')
 println ""
 h.step12('has-word-cumpleanos')
}

if ((args[0]) == 'step13') {
 println "Executing step13 (insert labels like has-word-cumpleanos)"
 h.step13('has-word-cumpleanos')
 println ""
 h.step13('has-root-frasco')
}

if ((args[0]) == 'step14') {
 println "Executing step14 (insert labels like has-root-decorar)"
 h.step14('has-root-decorar',
  ['decoro', 'decoras', 'decora', 'decoramos', 'decoraron'])
}

if ((args[0]) == 'step15') {
 println "Executing step15 (select labels for a certain sentence)"
 h.step15(43)
}

if ((args[0]) == 'step16') {
 println "Executing step16 (select sentences matching a certain sentence)"
 h.step16(34)
}

if ((args[0]) == 'step17') {
 println "Executing step17 (select sentences matching a certain sentence with user input)"
 h.step17(34, args[1])
}

if ((args[0]) == 'step17oneline') {
 println "Executing step17 (select sentences matching a certain sentence with user input one line)"
 h.step17oneline(34, args[1])
}
/*
 println "Executing step11 now (insert sample sentences and insert has-word labels)"
 h.step11(it)

 println "Executing step12 (select labels like has-root-poder)"
 h.step12('has-root-poder')
 h.step12('has-word-cumpleanos')

 println "Executing step13 (insert labels like has-word-cumpleanos)"
 h.step13('has-word-cumpleanos')
 h.step13('has-root-frasco')

 println "Executing step14 (insert labels like has-root-decorar)"
 h.step14('has-root-decorar',
  ['decoro', 'decoras', 'decora', 'decoramos', 'decoraron'])

 println "Executing step15 (select labels for a certain sentence)"
 h.step15(43)

 println "Executing step16 (select sentences matching a certain sentence)"
 h.step16(34)

 println "Executing step17 (select sentences matching a certain sentence with user input)"
 h.step17(34, args[1])

 println "Executing step17 (select sentences matching a certain sentence with user input one line)"
 h.step17oneline(34, args[1])
*/
