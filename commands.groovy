package example

import groovy.json.*
import groovyx.net.http.*

import groovy.io.FileType

import java.sql.*

class Hello {
 Connection conn
 def jsonSlurper
 def a0
 def a1
 def idOfSentenceJustSeen 

 def GREEN='\033[0;32m'
 def YELLOW='\033[0;33m'
 def NOCOLOR='\033[0m' 

 Hello() {
  Class.forName("org.h2.Driver");
  conn = DriverManager.
   getConnection("jdbc:h2:tcp://localhost/~/univision", "sa", "");
  jsonSlurper = new JsonSlurper()
 }

 def testStanfordStructure() {
  println "50.starting test"
  def answer = ['dave':null, 'kicked':null, 'the':null, 'ball':null]
  assert stanfordStructure('Dave kicked the ball') == answer
  assert stanfordStructure('Dave kicked the ball!') == answer
  assert stanfordStructure('Dave kicked the ball?') == answer
  assert stanfordStructure('Dave kicked the ball.') == answer
  println "50.test passed"
 }

 def stanfordStructure(String sentence) {
  def stanford = [:]
  sentence.split(' ').each {
   def temp = it
   temp = temp.replaceAll('[!?,\\.]','')
   temp = temp.toLowerCase()
   stanford[temp] = null
  } 

  def someday = 
  [
   "Dave"  :null,
   "kicked":["root":"kick"],
   "the"   :null,
   "ball"  :null,
  ]

  stanford
 }

 String convertWordListToInsertStatements(rs1key, HashMap a) {
  String t = ''
  a.each { k, v ->
   t += "insert into labels(sid, l) values($rs1key, 'has-word-$k');\n"
  }
  t
 }

 String getWordInHyphenedLabel(hyphenedLabel) {
  hyphenedLabel[-hyphenedLabel.reverse().indexOf('-')..-1]
 }

HashMap tokenizeUserInput(String line) {
 def argx = line.split(':')
 if ((argx.size() == 1) && (!(argx[0] =~ /^\d/))) {
  a0 = "18"
  a1 = argx[0]
 }
 if ((argx.size() == 1) && ((argx[0] =~ /^\d/))) {
  a0 = argx[0]
  a1 = ''
 }
 if (argx.size() > 1) {
  a0 = argx[0]
  a1 = argx[1]
 }
 [a0:a0, a1:a1]
}

//A line could come from:
// stdin
// a txt file
void interpretInputLine(line) {
def temp0 = tokenizeUserInput(line)
a0 = temp0.a0
a1 = temp0.a1

if (a0 == '0') {
 println "10: insert s {sentence}"
 println "13: insert l values hl {hyphenated label}//has-word-cumpleanos,has-root-frasco"
 println "12: select S where l=l //'has-root-poder','has-word-cumpleanos' "
 println "15: select L where s {}"
 println "19: select S where r {regexp}"
 println "20: select S where r limit 1 {regexp}"
 println "16: select S where s {}"
 println "24: select S where s limit 1 {}"
 println "17: select S where s and r {regexp}"
 println "18: select S where s and r limit 1 {regexp}"
 println "21: select s where s {}"
 println "22: insert values(s, l) {label}"
 println "23: delete values(l) from s {label}"
 println "25: delete s from s"
} 

if (a0 == '9') {
 //println "Executing step9 now (insert sample sentences and insert has-word labels)"
 step9()
}

if (a0 == '10') {
 String sentence = a1.trim()
 step11(sentence)
}

if (a0 == '12') {
 //println "12: select S where l='has-root-poder' {label} //or 'has-word-cumpleanos' "
 String label = a1
 step12(label)
}

if (a0 == '13') {
 //println "13: insert l values hl {hyphenated label} //has-word-cumpleanos,has-root-frasco"
 String label = a1
 step13(label)
}

if (a0 == '15') {
 //println "15: select L where s{}"
 step15(idOfSentenceJustSeen)
}

if (a0 == '16') {
 //println "16: select S where s"
 step16(idOfSentenceJustSeen)
}

if (a0 == '17') {
 //println "17: select S where s and r {regexp}"
 String regexp = a1
 step17(idOfSentenceJustSeen, regexp)
}

if (a0 =~ '18') {
 //println "18: select S where s and r limit 1 {regexp}"
 String regexp = a1
 boolean printSQL = (a0 =~ 'q')
 def temp = step18(printSQL, idOfSentenceJustSeen, regexp)
 if (temp.ids > 0) {
  idOfSentenceJustSeen = temp.ids
  String temp2 = getOutputline(temp.s, temp.l)
  println temp2
 }
}

if (a0 == '19') {
 //println "19: select S where r"
 String regexp = a1
 step19(regexp)
}

if (a0 == '20') {
 //println "20: select S where r limit 1"
 String regexp = a1
 def temp6 = step20(idOfSentenceJustSeen, regexp)
 if (temp6.ids > 0) {
  idOfSentenceJustSeen = temp6.ids
 }
 def temp = step21(idOfSentenceJustSeen)
}

if (a0 == '21') {
 //println "21: select s where s"
 step21(idOfSentenceJustSeen)
}

if (a0 == '22') {
 //println "22: insert values(s, l) {label}"
 String label = a1
 step22(idOfSentenceJustSeen, label)
}

if (a0 == '23') {
 //println "23: delete values(l) from s {label}"
 String label = a1
 step23(idOfSentenceJustSeen, label)
}

if (a0 == '24') {
 println "24: select S where s limit 1"
 def temp = step24(idOfSentenceJustSeen)
 //temp == -1: no sentence found
 //temp  >  0: exactly one sentence found
 if (temp.ids > 0) {
  idOfSentenceJustSeen = temp.ids
  String temp2 = getOutputline(temp.s, temp.l)
  println temp2
 }
}

if (a0 == '25') {
 step25(idOfSentenceJustSeen) 
} 

if (a0 == '99') {
 System.exit(0)
}
}

 void step9() {
  def f = new File('sentences.txt')
  f.each {
   //step11(it)
   interpretInputLine(it) 
  }
 }

 void step11(sentence) {
  String sql1 = "insert into sentences(s) values('$sentence')"
  def stmt1 = conn.prepareStatement(sql1, Statement.RETURN_GENERATED_KEYS)
  def x1 = stmt1.execute()
  ResultSet rs1 = stmt1.getGeneratedKeys()
  rs1.next()
  def rs1key = rs1.getInt(1) 

  def stanford = stanfordStructure(sentence)
  String sql2 = convertWordListToInsertStatements(rs1key, stanford)
  def stmt2 = conn.createStatement()
  try {
   def x2 = stmt2.execute(sql2)
  }
  catch (e) {
   println "insert failed"
   println e
  }
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
  //println sql4
  ResultSet rs4 = stmt4.executeQuery(sql4);

  def i=0
  while(rs4.next() && i++ <= 2000000) {
   String s = rs4.getString("s");

   println "$s"
  }
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

 void step15(int idOfSentenceJustSeen) {
  def stmt4 = conn.createStatement();
  String sql4 = ''
   //Find labels in a certain sentence
   sql4 += 'select l.l '
   sql4 += 'from labels l '
   sql4 += "where sid = $idOfSentenceJustSeen "
   sql4 += "and not l.l regexp 'has-word-' "

  ResultSet rs4 = stmt4.executeQuery(sql4);

  def i=0
  while(rs4.next() && i++ <= 2000000) {
   String l = rs4.getString("l");

   println "$l"
  }
 }

 void step16(int idOfSentenceJustSeen) {
  def stmt4 = conn.createStatement();
  String sql4 = ''
   //select S where s {}
   sql4 += 'select s.id ids, s.s, l.id idl, l.sid, l.l '
   sql4 += 'from sentences s '
   sql4 += 'join labels l '
   sql4 += 'on s.id = l.sid '
   sql4 += "where s.id <> $idOfSentenceJustSeen "
   sql4 += 'and l in ( '
   sql4 += 'select l.l '
   sql4 += 'from labels l '
   sql4 += "where sid = $idOfSentenceJustSeen "
   sql4 += ') '
   sql4 += 'order by l.l '
  //println sql4
  ResultSet rs4 = stmt4.executeQuery(sql4);

  def i=0
  while(rs4.next() && i++ <= 2000000) {
   String s   = rs4.getString("s");
   String l   = rs4.getString("l");
   println "$l,$s"
  }
 }

 void step17(int idOfSentenceJustSeen, String regexp) {
  def stmt4 = conn.createStatement();
  String sql4 = ''
   //Find sentences matching a certain sentence
   sql4 += 'select s.id ids, s.s, l.id idl, l.sid, l.l '
   sql4 += 'from sentences s '
   sql4 += 'join labels l '
   sql4 += 'on s.id = l.sid '
   sql4 += "where s.id <> $idOfSentenceJustSeen "
   sql4 += "and s.s regexp '$regexp' "
   sql4 += 'and l in ( '
   sql4 += 'select l.l '
   sql4 += 'from labels l '
   sql4 += "where sid = $idOfSentenceJustSeen "
   sql4 += ') '

  ResultSet rs4 = stmt4.executeQuery(sql4);

  def i=0
  while(rs4.next() && i++ <= 2000000) {
   String s   = rs4.getString("s");
   String l   = rs4.getString("l");

   println "$s,$l"
  }
 }

 HashMap step18(boolean printSQL, int idOfSentenceJustSeen, String regexp) {
  def stmt4 = conn.createStatement();
  String sql4 = ''
   //18: select S where s and r limit 1
   //Not an optimized query!
   sql4 += 'select * from (\n'
   sql4 += 'select rownum r, s.id ids, s.s, l.id idl, l.sid, l.l\n'
   sql4 += 'from sentences s\n'
   sql4 += 'join labels l\n'
   sql4 += 'on s.id = l.sid\n'
   sql4 += "where s.id <> $idOfSentenceJustSeen\n"
   sql4 += "and lower(s.s) regexp '$regexp'\n"
   sql4 += 'and l in (\n'
   sql4 += 'select l.l\n'
   sql4 += 'from labels l\n'
   sql4 += "where sid = $idOfSentenceJustSeen\n"
   sql4 += ')\n'
   sql4 += ')\n'
   sql4 += 'order by rand()\n'
   sql4 += 'limit 1\n'
  if (printSQL) println sql4
  ResultSet rs4 = stmt4.executeQuery(sql4);

  int ids = -1
  String s
  String l
  while(rs4.next()) {
   ids = rs4.getInt("ids");
   s   = rs4.getString("s");
   l   = rs4.getString("l");
  }
  [ids:ids, s:s, l:l]
 }

 void step19(String regexp) {
  def stmt4 = conn.createStatement();
  String sql4 = ''
   //19: select S where r
   //Not an optimized query!
   sql4 += 'select * from ( '
   sql4 += 'select rownum r, s.id ids, s.s '
   sql4 += 'from sentences s '
   sql4 += "where s.s regexp '$regexp' "
   sql4 += ') '
   sql4 += 'order by rand() '

  ResultSet rs4 = stmt4.executeQuery(sql4);

  String r
  int ids
  String s
  String idl
  String sid
  String l
  def i=0
  while(rs4.next() && i++ <= 2000000) {
   s   = rs4.getString("s");
   println "$s"
  }
 }

 HashMap step20(int idOfSentenceJustSeen, String regexp) {
  def stmt4 = conn.createStatement();
  String sql4 = ''
   //20: select S where r limit 1
   //Not an optimized query!
   sql4 += 'select * from ( '
   sql4 += 'select rownum r, s.id ids, s.s '
   sql4 += 'from sentences s '
   sql4 += "where s.s regexp '$regexp' "
   sql4 += ') '
   sql4 += 'order by rand() '
   sql4 += 'limit 1 '

  ResultSet rs4 = stmt4.executeQuery(sql4);

  int ids = -1
  String s
  while(rs4.next()) {
   ids = rs4.getInt("ids");
   s   = rs4.getString("s");
  }

  [ids:ids, s:s, l:'we are matching a regexp not the current sentence']
 }

 void step21(int idOfSentenceJustSeen) {
  def stmt4 = conn.createStatement();
  String sql4 = ''
   //21: select s where s
   sql4 += 'select s.s '
   sql4 += 'from sentences s '
   sql4 += "where s.id = $idOfSentenceJustSeen "

  ResultSet rs4 = stmt4.executeQuery(sql4);

  String r
  String s
  while(rs4.next()) {
   s   = rs4.getString("s");
   println "$GREEN$s$NOCOLOR"
  }
 }

 void step22(int idOfSentenceJustSeen, String label) {
  String sql2 = "insert into labels(sid, l) values ($idOfSentenceJustSeen, '$label')"
  def stmt2 = conn.createStatement()
  try {
   def x2 = stmt2.execute(sql2)
  }
  catch (e) {
   println "insert failed"
   println e
  }
 }

 void step23(int idOfSentenceJustSeen, String label) {
  String sql2
  sql2  = "delete from labels "
  sql2 += "where sid = $idOfSentenceJustSeen "
  sql2 += "and l = '$label' "
  def stmt2 = conn.createStatement()
  try {
   def x2 = stmt2.execute(sql2)
  }
  catch (e) {
   println "insert failed"
   println e
  }
 }

 HashMap step24(int idOfSentenceJustSeen) {
  def stmt4 = conn.createStatement();
  String sql4 = ''
   //select S where s limit 1
   sql4 += 'select s.id ids, s.s, l.id idl, l.sid, l.l\n'
   sql4 += 'from sentences s\n'
   sql4 += 'join labels l\n'
   sql4 += 'on s.id = l.sid\n'
   sql4 += "where s.id <> $idOfSentenceJustSeen\n"
   sql4 += 'and l in (\n'
   sql4 += 'select l.l\n'
   sql4 += 'from labels l\n'
   sql4 += "where sid = $idOfSentenceJustSeen\n"
   sql4 += ')\n'
   sql4 += 'order by rand() '
   sql4 += 'limit 1\n'
  ResultSet rs4 = stmt4.executeQuery(sql4);

  int ids = -1
  String s
  String l
  while(rs4.next()) {
   ids        = rs4.getInt("ids");
   s   = rs4.getString("s");
   l   = rs4.getString("l");
  }
  [ids:ids, s:s, l:l]
 }

 void step25(int idOfSentenceJustSeen) {
  String sql2
  sql2  = "delete from labels "
  sql2 += "where sid = $idOfSentenceJustSeen; "
  sql2 += "delete from sentences "
  sql2 += "where  id = $idOfSentenceJustSeen  "
  def stmt2 = conn.createStatement()
  try {
   def x2 = stmt2.execute(sql2)
  }
  catch (e) {
   println "deletes failed"
   println e
  }
  println "Deleted sentence $idOfSentenceJustSeen"
 }

 def testGetOutputline() {
  println "246.starting test"
  def answer = "${GREEN}Un dia especial para ti y todas las bellas mujeres del ${YELLOW}mundo${GREEN} ${NOCOLOR}"
  def temp = 'Un dia especial para ti y todas las bellas mujeres del mundo'
  assert getOutputline(temp, 'has-word-mundo') == answer

  answer = "${GREEN}Felizmente me quedo con ${YELLOW}esta${GREEN} vista, con esta paz y tranquilidad que solo ofrece Castel Gandolfo ${NOCOLOR}"
  assert getOutputline('Felizmente me quedo con esta vista, con esta paz y tranquilidad que solo ofrece Castel Gandolfo', 'has-word-esta')

  println "246.test passed"
 }

 String getOutputline(String sentence, String label) {
   String templabel = getWordInHyphenedLabel(label)

   sentence += ' ' //enables matching the final word

   int templabelindexof 
   templabelindexof = sentence.indexOf(" $templabel ")

   String outputline
   if (templabelindexof < 1) {
    outputline = "$GREEN$sentence$NOCOLOR"
   }
   else {
    outputline  =   "GREEN${sentence[0..templabelindexof]}"
    outputline +=   "YELLOW${templabel}"
    outputline +=   "GREEN${sentence[templabelindexof+templabel.size()+1..-1]}"
    outputline +=   "NOCOLOR"
    //println outputline
    outputline  =   "$GREEN${sentence[0..templabelindexof]}"
    outputline +=   "$YELLOW$templabel"
    outputline +=   "$GREEN${sentence[templabelindexof+templabel.size()+1..-1]}"
    outputline +=   "$NOCOLOR"
   }
   outputline
 }
}

def NO_PREVIOUS_SENTENCE = -1

def h = new Hello()
h.testStanfordStructure()
h.testGetOutputline()
def DUMMY = -1
def checkForSampleData = h.step20(DUMMY, 'sonrisa.*mundo')
if (checkForSampleData.ids == -1) {
 h.step9()
}
else {
 testUserInputSequence(h)
}

def testUserInputSequence(Hello h) {
  println "437.starting test"

  String abc = 'ABC'
  int code
  code = (int)abc[0]

  def DUMMY = -1
  def tmp = h.step20(DUMMY, 'sonrisa.*mundo')
  def temp = h.step18(false, tmp.ids, 'bellas mujeres') 
  String answer = "${h.GREEN}Un dia especial para ti y todas las bellas mujeres del ${h.YELLOW}mundo${h.GREEN} ${h.NOCOLOR}"
  String temp2 = h.getOutputline(temp.s, temp.l)

  assert temp2 == answer
  println "437.test passed"
}

//Assumes there exists at least one sentence
def temp4 = h.step20(NO_PREVIOUS_SENTENCE, '.*')
h.idOfSentenceJustSeen = temp4.ids
h.step21(h.idOfSentenceJustSeen)

System.in.eachLine() { line ->  
 h.interpretInputLine(line) 
}

/* s/m: Use plagiarism algorithm */
