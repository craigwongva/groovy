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
   a0 = "34"
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
   println "31: select S where s"
   println "32: select S where s limit 1"
   println "33: select S where s and r"
   println "34: select S where s and r limit 1"
   println "35: select S where r"
   println "36: select S where r limit 1"
   println "37: select S where l"
   println "38: select L where s"
   println "39: select s where s"
   println "40: insert s"
   println "41: delete s"
   println "42: insert s, l"
   println "43: delete s, l"

   println "87: insert l" //what is the use case?
  } 
  
  if (a0 == '9') {
   step9_insertSampleSentences()
  }
  if (a0 == '31') {
   def a = step31_select_S_where_s(idOfSentenceJustSeen)
   step31_print(a)
  }
  if (a0 == '32') {
   HashMap h = 
    step32_select_S_from_s_limit_1(
     idOfSentenceJustSeen)
   if (h.ids > 0) {
    idOfSentenceJustSeen = h.ids
   }
   step32_print(h)
  }
  if (a0 == '33') {
   String regexp = a1
   ArrayList a = step33_select_S_where_s_and_r(idOfSentenceJustSeen, regexp)
   step33_print(a)
  }
  if (a0 == '34') {
   String regexp = a1
   HashMap h = 
    step34_select_S_where_s_and_r_limit_1(
     idOfSentenceJustSeen, regexp)
   if (h.ids > 0) {
    idOfSentenceJustSeen = h.ids
   }
   step34_print(h)
  }
  if (a0 == '35') {
   String regexp = a1
   ArrayList a = step35_select_S_where_r(regexp)
   step35_print(a)
  }
  if (a0 == '36') {
   String regexp = a1
   HashMap h = step36_select_S_where_r_limit_1(regexp)
   if (h.ids > 0) {
    idOfSentenceJustSeen = h.ids
   }
   step36_print(idOfSentenceJustSeen)
  }
  if (a0 == '37') {
   String label = a1
   ArrayList a = step37_select_S_where_l(label)
   step37_print(a)
  }
  if (a0 == '38') {
   ArrayList a = step38_select_L_where_s(idOfSentenceJustSeen)
   step38_print(a)
  }
  if (a0 == '39') {
   String s = step39_select_s_where_s_print(idOfSentenceJustSeen)
   step39_print(s)
  }
  if (a0 == '40') {
   String sentence = a1.trim()
   step40_insert_s(sentence)
  }
  if (a0 == '41') {
   step41_delete_s(idOfSentenceJustSeen) 
  } 
  if (a0 == '42') {
   String label = a1
   step42_insert_s_and_l(idOfSentenceJustSeen, label)
  }
  if (a0 == '43') {
   String label = a1
   step43_delete_s_and_l(idOfSentenceJustSeen, label)
  }  
  if (a0 == '87') {
   String label = a1
   step87_insert_l(label)
  }
  if (a0 == '99') {
   System.exit(0)
  }
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

 void step9_insertSampleSentences() {
  def f = new File('sentences.txt')
  f.each {
   interpretInputLine(it) 
  }
 }

 ArrayList step31_select_S_where_s(int idOfSentenceJustSeen) {
  String q = ''
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

  def stmt = conn.createStatement()
  ResultSet rs = stmt.executeQuery(q)

  def i=0
  def a = []
  while(rs.next() && i++ <= 2000000) {
    String s   = rs.getString("s")
    String l   = rs.getString("l")
    a << [s, l]
  }
  a
 }

 HashMap step32_select_S_from_s_limit_1(int idOfSentenceJustSeen) {
  String q = ''
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
  q += 'order by rand() '
  q += 'limit 1 '

  def stmt = conn.createStatement();
  ResultSet rs = stmt.executeQuery(q);

  int ids = -1
  String s
  String l
  rs.next()
  ids = rs.getInt("ids");
  s   = rs.getString("s");
  l   = rs.getString("l");
  [ids:ids, s:s, l:l]
 }

 ArrayList step33_select_S_where_s_and_r(
  int idOfSentenceJustSeen, String regexp) {

  String q = ''
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

  def i = 0
  def a = []
  while(rs.next() && i++ <= 2000000) {
    String s   = rs.getString("s");
    String l   = rs.getString("l");
    a << [s, l]
  }
  a
 }

 HashMap step34_select_S_where_s_and_r_limit_1(
  int idOfSentenceJustSeen, String regexp) {

  String q = ''
  //Not an optimized query!
  q += 'select * from ( '
  q += 'select rownum r, s.id ids, s.s, l.id idl, l.sid, l.l '
  q += 'from sentences s '
  q += 'join labels l '
  q += 'on s.id = l.sid '
  q += "where s.id <> $idOfSentenceJustSeen "
  q += "and lower(s.s) regexp '$regexp' "
  q += 'and l in ( '
  q += 'select l.l '
  q += 'from labels l '
  q += "where sid = $idOfSentenceJustSeen "
  q += ') '
  q += ') '
  q += 'order by rand() '
  q += 'limit 1'
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

 ArrayList step35_select_S_where_r(String regexp) {
  String q = ''
  //Not an optimized query!
  q += 'select * from ( '
  q += 'select rownum r, s.id ids, s.s '
  q += 'from sentences s '
  q += "where s.s regexp '$regexp' "
  q += ') '
  q += 'order by rand() '

  def stmt = conn.createStatement();
  ResultSet rs = stmt.executeQuery(q);

  String s
  def i=0
  def a = []
  while(rs.next() && i++ <= 2000000) {
   s   = rs.getString("s");
   a << s
  }
  a
 }

 HashMap step36_select_S_where_r_limit_1(String regexp) {
  String q = ''
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

  [ids:ids, s:s]
 }

 ArrayList step37_select_S_where_l(String hyphenedLabel) {
  String q = ''
  q += 'select s.* '
  q += 'from sentences s '
  q += 'join labels l '
  q += 'on s.id = l.sid '
  q += "where l = '$hyphenedLabel' "

  def stmt = conn.createStatement();
  ResultSet rs = stmt.executeQuery(q);

  def i=0
  def a = []
  while(rs.next() && i++ <= 2000000) {
    a << rs.getString("s");
  }
  a
 }

 ArrayList step38_select_L_where_s(int idOfSentenceJustSeen) {
  String q = ''
  q += 'select l.l '
  q += 'from labels l '
  q += "where sid = $idOfSentenceJustSeen "
  q += "and not l.l regexp 'has-word-' "

  def stmt = conn.createStatement();
  ResultSet rs = stmt.executeQuery(q);

  def i=0
  def a = []
  while(rs.next() && i++ <= 2000000) {
    a << rs.getString("l");
  }
  a
 }

 String step39_select_s_where_s_print(int idOfSentenceJustSeen) {
  String q = ''
  q += 'select s.s '
  q += 'from sentences s '
  q += "where s.id = $idOfSentenceJustSeen "
  def stmt = conn.createStatement()
  ResultSet rs = stmt.executeQuery(q)

  rs.next() 
  String s = rs.getString("s")
 }

 void step40_insert_s(String sentence) {
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

 void step41_delete_s(int idOfSentenceJustSeen) {
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
  println "--Deleted sentence--"
 }

 void step42_insert_s_and_l(int idOfSentenceJustSeen, String label) {
  //bug: allows duplicate insertion of a sentence/label combination
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

 void step43_delete_s_and_l(int idOfSentenceJustSeen, String label) {
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

 void step87_insert_l(String hyphenedLabel) {
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

 void step31_print(ArrayList a) {
  if (a == []) {
   println "--no results--"
   return
  }

  a.each {
   String s = it[0]
   String l = it[1]
   println "$l,$s"
  }
 }

 void step32_print(HashMap h) {
  step34_print(h)
 }

 void step33_print(ArrayList a) {
  if (a == []) {
   println "--no results--"
   return
  }

  a.each {
   String s = it[0]
   String l = it[1]
   println "$s,$l"
  }
 }

 void step34_print(HashMap h) {
  if (h.ids > 0) {
   idOfSentenceJustSeen = h.ids
   println getOutputline(h.s, h.l)
  }
  else {
   println "--no sentence change--"
  }
 }

 void step35_print(ArrayList a) {
  step37_print(a)
 } 

 void step36_print(int idOfSentenceJustSeen) {
  String s = step39_select_s_where_s_print(idOfSentenceJustSeen)
  step39_print(s)
 }

 void step37_print(ArrayList a) {
  if (a.size() == 0) {
   println "--no results--"
  }
  else {
   a.each {
    println it
   }
  }
 }

 void step38_print(ArrayList a) {
  step37_print(a)
 } 

 void step39_print(String s) {
  println "$GREEN$s$NOCOLOR"
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
  h.step36_select_S_where_r_limit_1(arbitraryExpectedString)
 if (temp.ids == -1) {
  h.step9_insertSampleSentences()
 }
}

void runIntegrationTests(Yacback h) {
 testUserInputSequence1(h)
}

def testUserInputSequence1(Yacback h) {
 def temp1 = h.step36_select_S_where_r_limit_1('sonrisa.*mundo')
 def temp2 = h.step34_select_S_where_s_and_r_limit_1(temp1.ids, 'bellas mujeres') 
 String answer = "${h.GREEN}Un dia especial para ti y todas las bellas mujeres del ${h.YELLOW}mundo${h.GREEN} ${h.NOCOLOR}"
 String temp3 = h.getOutputline(temp2.s, temp2.l)
 assert temp3 == answer

 println "testUserInputSequence1 passed"
}

void getAnInitialSentence(Yacback h) {
 HashMap temp = h.step36_select_S_where_r_limit_1('.*')
 h.idOfSentenceJustSeen = temp.ids
 String s = h.step39_select_s_where_s_print(h.idOfSentenceJustSeen)
 h.step39_print(s)
}

void inviteUserInputForever(Yacback h) {
 System.in.eachLine() { line ->  
  h.interpretInputLine(line) 
 }
}
