package yacback

import groovy.json.*
import groovyx.net.http.*

import groovy.io.FileType

import java.sql.*

class Yacback {
 Connection conn
 def jsonSlurper
 def a0
 def a1
 def idOfSentenceJustSeen 

 def GREEN='\033[0;32m'
 def YELLOW='\033[0;33m'
 def NOCOLOR='\033[0m' 

 Yacback() {
  Class.forName("org.h2.Driver");
  conn = DriverManager.
   getConnection("jdbc:h2:tcp://localhost/~/univision", "sa", "");
  jsonSlurper = new JsonSlurper()
 }

 def testStanfordStructure() {
  def answer = ['dave':null, 'kicked':null, 'the':null, 'ball':null]
  assert stanfordStructure('Dave kicked the ball') == answer
  assert stanfordStructure('Dave kicked the ball!') == answer
  assert stanfordStructure('Dave kicked the ball?') == answer
  assert stanfordStructure('Dave kicked the ball.') == answer
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

ArrayList tokenizeUserInput(String line) {
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

 [a0, a1]
}

//A line could come from:
// stdin
// a txt file
void interpretInputLine(line) {
  def (a0, a1) = tokenizeUserInput(line)

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
 step9_insertSampleSentences()
}

if (a0 == '10') {
 String sentence = a1.trim()
 step11_insert_s(sentence)
}

if (a0 == '12') {
 //has-root-poder, has-word-cumpleanos
 String label = a1
 step12_select_S_where_l(label)
}

if (a0 == '13') {
 //has-word-cumpleanos, has-root-frasco
 String label = a1
 step13_insert_l(label)
}

if (a0 == '15') {
 step15_select_L_where_s(idOfSentenceJustSeen)
}

if (a0 == '16') {
 step16_select_S_where_s(idOfSentenceJustSeen)
}

if (a0 == '17') {
 String regexp = a1
 step17_select_S_where_s_and_r(idOfSentenceJustSeen, regexp)
}

if (a0 =~ '18') {
 String regexp = a1
 HashMap temp = step18_select_S_where_s_and_r_limit_1(idOfSentenceJustSeen, regexp)
 if (temp.ids > 0) {
  idOfSentenceJustSeen = temp.ids
  println getOutputline(temp.s, temp.l)
 }
}

if (a0 == '19') {
 String regexp = a1
 step19_select_S_where_r(regexp)
}

if (a0 == '20') {
 String regexp = a1
 HashMap temp = step20_select_S_where_r_limit_1(regexp)
 if (temp.ids > 0) {
  idOfSentenceJustSeen = temp.ids
 }
 step21_select_s_where_s_print(idOfSentenceJustSeen)
}

if (a0 == '21') {
 step21_select_s_where_s_print(idOfSentenceJustSeen)
}

if (a0 == '22') {
 String label = a1
 step22_insert_values_s_l(idOfSentenceJustSeen, label)
}

if (a0 == '23') {
 String label = a1
 step23_delete_L_from_s(idOfSentenceJustSeen, label)
}

if (a0 == '24') {
 HashMap temp = step24_select_S_from_s_limit_1(idOfSentenceJustSeen)
 if (temp.ids > 0) {
  idOfSentenceJustSeen = temp.ids
  println getOutputline(temp.s, temp.l)
 }
}

if (a0 == '25') {
 step25_delete_s(idOfSentenceJustSeen) 
} 

if (a0 == '99') {
 System.exit(0)
}
}

 void step9_insertSampleSentences() {
  def f = new File('sentences.txt')
  f.each {
   interpretInputLine(it) 
  }
 }

 void step11_insert_s(String sentence) {
   String q1 = "insert into sentences(s) values('$sentence')"
   def stmt1 = conn.prepareStatement(q1, Statement.RETURN_GENERATED_KEYS)
   try {
     stmt1.execute()
   }
   catch (e) {
     println "insert1 failed"
     println e
   }
   ResultSet rs1 = stmt1.getGeneratedKeys()
   rs1.next()
   def rs1key = rs1.getInt(1) 

   def stanford = stanfordStructure(sentence)
   String q2 = convertWordListToInsertStatements(rs1key, stanford)
   def stmt2 = conn.createStatement()
   try {
     stmt2.execute(q2)
   }
   catch (e) {
     println "insert2 failed"
     println e
   }
 }

 void step12_select_S_where_l(String hyphenedLabel) {
   String q = ''
   q += 'select s.* '
   q += 'from sentences s '
   q += 'join labels l '
   q += 'on s.id = l.sid '
   q += "where l = '$hyphenedLabel' "

   def stmt = conn.createStatement();
   ResultSet rs = stmt.executeQuery(q);

   def i=0
   while(rs.next() && i++ <= 2000000) {
     String s = rs.getString("s");

     println s
   }
 }

 void step13_insert_l(String hyphenedLabel) {
  String wordInHyphenedLabel =
   getWordInHyphenedLabel(hyphenedLabel)

  String q = ''
  q += "delete from labels where l = '$hyphenedLabel'; "
  q += 'insert into labels (sid, l) '
  q += "select id, '$hyphenedLabel' "
  q += 'from sentences s '
  q += "where s.s regexp '$wordInHyphenedLabel' "

  def stmt = conn.createStatement();
  stmt.execute(q);
 }

 void step15_select_L_where_s(int idOfSentenceJustSeen) {
   String q = ''
   //Find labels in a certain sentence
   q += 'select l.l '
   q += 'from labels l '
   q += "where sid = $idOfSentenceJustSeen "
   q += "and not l.l regexp 'has-word-' "

   def stmt = conn.createStatement();
   ResultSet rs = stmt.executeQuery(q);

   def i=0
   while(rs.next() && i++ <= 2000000) {
     String l = rs.getString("l");

     println "$l"
   }
 }

 void step16_select_S_where_s(int idOfSentenceJustSeen) {
   String q = ''
   //select S where s {}
   q += 'select s.id ids, s.s, l.id idl, l.sid, l.l '
   q += 'from sentences s '
   q += 'join labels l '
   q += 'on s.id = l.sid '
   q += "where s.id <> $idOfSentenceJustSeen "
   q += 'and l in ( '
   q += 'select l.l '
   q += 'from labels l '
   q += "where sid = $idOfSentenceJustSeen "
   q += ') '
   q += 'order by l.l '
 
   def stmt = conn.createStatement();
   ResultSet rs = stmt.executeQuery(q);

   def i=0
   while(rs.next() && i++ <= 2000000) {
     String s   = rs.getString("s");
     String l   = rs.getString("l");
     println "$l,$s"
   }
 }

 void step17_select_S_where_s_and_r(int idOfSentenceJustSeen, String regexp) {
   String q = ''
   //Find sentences matching a certain sentence
   q += 'select s.id ids, s.s, l.id idl, l.sid, l.l '
   q += 'from sentences s '
   q += 'join labels l '
   q += 'on s.id = l.sid '
   q += "where s.id <> $idOfSentenceJustSeen "
   q += "and s.s regexp '$regexp' "
   q += 'and l in ( '
   q += 'select l.l '
   q += 'from labels l '
   q += "where sid = $idOfSentenceJustSeen "
   q += ') '

   def stmt = conn.createStatement();
   ResultSet rs = stmt.executeQuery(q);

   def i=0
   while(rs.next() && i++ <= 2000000) {
     String s   = rs.getString("s");
     String l   = rs.getString("l");

     println "$s,$l"
   }
 }

 HashMap step18_select_S_where_s_and_r_limit_1(int idOfSentenceJustSeen, String regexp) {
   String q = ''
   //18: select S where s and r limit 1
   //Not an optimized query!
   q += 'select * from (\n'
   q += 'select rownum r, s.id ids, s.s, l.id idl, l.sid, l.l\n'
   q += 'from sentences s\n'
   q += 'join labels l\n'
   q += 'on s.id = l.sid\n'
   q += "where s.id <> $idOfSentenceJustSeen\n"
   q += "and lower(s.s) regexp '$regexp'\n"
   q += 'and l in (\n'
   q += 'select l.l\n'
   q += 'from labels l\n'
   q += "where sid = $idOfSentenceJustSeen\n"
   q += ')\n'
   q += ')\n'
   q += 'order by rand()\n'
   q += 'limit 1\n'
  def stmt = conn.createStatement();
  ResultSet rs = stmt.executeQuery(q);

  int ids = -1
  String s
  String l
  while(rs.next()) {
   ids = rs.getInt("ids");
   s   = rs.getString("s");
   l   = rs.getString("l");
  }
  [ids:ids, s:s, l:l]
 }

 void step19_select_S_where_r(String regexp) {
   String q = ''
   //19: select S where r
   //Not an optimized query!
   q += 'select * from ( '
   q += 'select rownum r, s.id ids, s.s '
   q += 'from sentences s '
   q += "where s.s regexp '$regexp' "
   q += ') '
   q += 'order by rand() '

   def stmt = conn.createStatement();
   ResultSet rs4 = stmt.executeQuery(q);

   String s
   def i=0
   while(rs4.next() && i++ <= 2000000) {
    s   = rs4.getString("s");
    println s
   }
 }

 HashMap step20_select_S_where_r_limit_1(String regexp) {
   String q = ''
   //20: select S where r limit 1
   //Not an optimized query!
   q += 'select * from ( '
   q += 'select rownum r, s.id ids, s.s '
   q += 'from sentences s '
   q += "where s.s regexp '$regexp' "
   q += ') '
   q += 'order by rand() '
   q += 'limit 1 '

  def stmt = conn.createStatement();
  ResultSet rs = stmt.executeQuery(q);

  int ids = -1
  String s
  rs.next() 
  ids = rs.getInt("ids");
  s   = rs.getString("s");

  [ids:ids, s:s, l:'we are matching a regexp not the current sentence']
 }

 void step21_select_s_where_s_print(int idOfSentenceJustSeen) {
  //21: select s where s
  String q = ''
  q += 'select s.s '
  q += 'from sentences s '
  q += "where s.id = $idOfSentenceJustSeen "
  def stmt = conn.createStatement()
  ResultSet rs = stmt.executeQuery(q)

  rs.next() 
  String s = rs.getString("s")
  println "$GREEN$s$NOCOLOR"
 }

 void step22_insert_values_s_l(int idOfSentenceJustSeen, String label) {
  String q
  q  = "insert into labels(sid, l) "
  q += "values ($idOfSentenceJustSeen, '$label')"
  def stmt = conn.createStatement()
  try {
   stmt.execute(q)
  }
  catch (e) {
   println "insert failed"
   println e
  }
 }

 void step23_delete_L_from_s(int idOfSentenceJustSeen, String label) {
  String q
  q  = "delete from labels "
  q += "where sid = $idOfSentenceJustSeen "
  q += "and l = '$label' "
  def stmt = conn.createStatement()
  try {
   stmt.execute(q)
  }
  catch (e) {
   println "insert failed"
   println e
  }
 }

 HashMap step24_select_S_from_s_limit_1(int idOfSentenceJustSeen) {
  def stmt = conn.createStatement();
  String q = ''
  //select S where s limit 1
  q += 'select s.id ids, s.s, l.id idl, l.sid, l.l\n'
  q += 'from sentences s\n'
  q += 'join labels l\n'
  q += 'on s.id = l.sid\n'
  q += "where s.id <> $idOfSentenceJustSeen\n"
  q += 'and l in (\n'
  q += 'select l.l\n'
  q += 'from labels l\n'
  q += "where sid = $idOfSentenceJustSeen\n"
  q += ')\n'
  q += 'order by rand() '
  q += 'limit 1\n'
  ResultSet rs = stmt.executeQuery(q);

  int ids = -1
  String s
  String l
  while(rs.next()) {
   ids        = rs.getInt("ids");
   s   = rs.getString("s");
   l   = rs.getString("l");
  }
  [ids:ids, s:s, l:l]
 }

 void step25_delete_s(int idOfSentenceJustSeen) {
  String s
  s  = "delete from labels "
  s += "where sid = $idOfSentenceJustSeen; "
  s += "delete from sentences "
  s += "where  id = $idOfSentenceJustSeen  "
  def stmt = conn.createStatement()
  try {
   stmt.execute(s)
  }
  catch (e) {
   println "deletes failed"
   println e
  }
  println "Deleted sentence $idOfSentenceJustSeen"
 }

 String getOutputline(String sentence, String label) {
   String templabel = getWordInHyphenedLabel(label)

   sentence += ' ' //enables matching the final word

   int templabelindexof 
   templabelindexof = sentence.indexOf(" $templabel ")

   String s
   if (templabelindexof < 1) {
    s = "$GREEN$sentence$NOCOLOR"
   }
   else {
    s  =   "$GREEN${sentence[0..templabelindexof]}"
    s +=   "$YELLOW$templabel"
    s +=   "$GREEN${sentence[templabelindexof+templabel.size()+1..-1]}"
    s +=   "$NOCOLOR"
   }
   s
 }

 void testGetOutputline() {
  def answer = "${GREEN}Un dia especial para ti y todas las bellas mujeres del ${YELLOW}mundo${GREEN} ${NOCOLOR}"
  def temp = 'Un dia especial para ti y todas las bellas mujeres del mundo'
  assert getOutputline(temp, 'has-word-mundo') == answer

  answer = "${GREEN}Felizmente me quedo con ${YELLOW}esta${GREEN} vista, con esta paz y tranquilidad que solo ofrece Castel Gandolfo ${NOCOLOR}"
  assert getOutputline('Felizmente me quedo con esta vista, con esta paz y tranquilidad que solo ofrece Castel Gandolfo', 'has-word-esta')

  println "testGetOutputline passed"
 }
}

def h = new Yacback()
ensureSampleDataAndRunTests(h)
getAnInitialSentence(h) 
inviteUserInputForever(h)

void ensureSampleDataAndRunTests(Yacback h) {
 runUnitTests(h)
 ensureSampleData(h)
 runIntegrationTests(h)
}

void runUnitTests(Yacback h) {
 h.testStanfordStructure()
 h.testGetOutputline()
}

void ensureSampleData(Yacback h) {
 String arbitraryExpectedString = 'sonrisa.*mundo'
 HashMap temp = 
  h.step20_select_S_where_r_limit_1(arbitraryExpectedString)
 if (temp.ids == -1) {
  h.step9_insertSampleSentences()
 }
}

void runIntegrationTests(Yacback h) {
 testUserInputSequence1(h)
}

def testUserInputSequence1(Yacback h) {
  def temp1 = h.step20_select_S_where_r_limit_1('sonrisa.*mundo')
  def temp2 = h.step18_select_S_where_s_and_r_limit_1(temp1.ids, 'bellas mujeres') 
  String answer = "${h.GREEN}Un dia especial para ti y todas las bellas mujeres del ${h.YELLOW}mundo${h.GREEN} ${h.NOCOLOR}"
  String temp3 = h.getOutputline(temp2.s, temp2.l)
  assert temp3 == answer

  println "testUserInputSequence1 passed"
}

void getAnInitialSentence(Yacback h) {
 HashMap temp = h.step20_select_S_where_r_limit_1('.*')
 h.idOfSentenceJustSeen = temp.ids
 h.step21_select_s_where_s_print(h.idOfSentenceJustSeen)
}

void inviteUserInputForever(Yacback h) {
 System.in.eachLine() { line ->  
  h.interpretInputLine(line) 
 }
}
