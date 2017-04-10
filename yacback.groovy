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
 step9_insertSampleSentences()
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
 HashMap temp = step18(idOfSentenceJustSeen, regexp)
 if (temp.ids > 0) {
  idOfSentenceJustSeen = temp.ids
  println getOutputline(temp.s, temp.l)
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
 HashMap temp = step20_select_S_where_r_limit_1(regexp)
 if (temp.ids > 0) {
  idOfSentenceJustSeen = temp.ids
 }
 step21_select_s_where_s_print(idOfSentenceJustSeen)
}

if (a0 == '21') {
 //println "21: select s where s"
 step21_select_s_where_s_print(idOfSentenceJustSeen)
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

 HashMap step18(int idOfSentenceJustSeen, String regexp) {
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

 HashMap step20_select_S_where_r_limit_1(String regexp) {
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

 void step21_select_s_where_s_print(int idOfSentenceJustSeen) {
  def stmt4 = conn.createStatement();
  String sql4 = ''
  //21: select s where s
  sql4 += 'select s.s '
  sql4 += 'from sentences s '
  sql4 += "where s.id = $idOfSentenceJustSeen "

  ResultSet rs4 = stmt4.executeQuery(sql4);

  rs4.next() 
  String s = rs4.getString("s");
  println "$GREEN$s$NOCOLOR"
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

 HashMap step24_select_S_from_s_limit_1(int idOfSentenceJustSeen) {
  def stmt = conn.createStatement();
  String sql = ''
  //select S where s limit 1
  sql += 'select s.id ids, s.s, l.id idl, l.sid, l.l\n'
  sql += 'from sentences s\n'
  sql += 'join labels l\n'
  sql += 'on s.id = l.sid\n'
  sql += "where s.id <> $idOfSentenceJustSeen\n"
  sql += 'and l in (\n'
  sql += 'select l.l\n'
  sql += 'from labels l\n'
  sql += "where sid = $idOfSentenceJustSeen\n"
  sql += ')\n'
  sql += 'order by rand() '
  sql += 'limit 1\n'
  ResultSet rs = stmt.executeQuery(sql);

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
  def temp2 = h.step18(temp1.ids, 'bellas mujeres') 
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
