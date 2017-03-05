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

 void step11(sentence) {
  String sql1 = "insert into sentences(s) values('$sentence')"
  def stmt1 = conn.prepareStatement(sql1, Statement.RETURN_GENERATED_KEYS)
  def x1 = stmt1.execute()
  ResultSet rs1 = stmt1.getGeneratedKeys()
  rs1.next()
  def rs1key = rs1.getInt(1) 

  def stanford = convertSentenceToStanfordStructure(sentence)
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


 def convertSentenceToStanfordStructure(String s) {
  def stanford = [:]
  s.split(' ').each {
   stanford[it] = null
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
  def i=0
  while(rs4.next() && i++ <= 2000000) {
   s   = rs4.getString("s");
   println "$s"
  }
 }

 int step20(String regexp) {
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

  String r
  int ids = -1
  String s
  def i=0
  while(rs4.next() && i++ <= 2000000) {
   ids = rs4.getInt("ids");
   s   = rs4.getString("s");
   println "$s"
  }
  return ids
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

 int step18(boolean printSQL, int idOfSentenceJustSeen, String regexp) {
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
   sql4 += "and s.s regexp '$regexp'\n"
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

  String r
  int ids = -1
  String s
  String l
  def i=0
  while(rs4.next() && i++ <= 2000000) {
   ids = rs4.getInt("ids");
   s   = rs4.getString("s");
   l   = rs4.getString("l");

   println "$s,$l"
  }
  return ids
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

 int step24(int idOfSentenceJustSeen) {
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
  println sql4
  ResultSet rs4 = stmt4.executeQuery(sql4);

  int ids = -1
  def i=0
  while(rs4.next() && i++ <= 2000000) {
   ids        = rs4.getInt("ids");
   String s   = rs4.getString("s");
   String l   = rs4.getString("l");

   println "$s,$l"
  }
  return ids
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
  println sql4
  ResultSet rs4 = stmt4.executeQuery(sql4);

  def i=0
  while(rs4.next() && i++ <= 2000000) {
   String s   = rs4.getString("s");
   String l   = rs4.getString("l");
   println "$s,$l"
  }
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
  println sql4
  ResultSet rs4 = stmt4.executeQuery(sql4);

  def i=0
  while(rs4.next() && i++ <= 2000000) {
   String s = rs4.getString("s");

   println "$s"
  }
 }

}

def h = new Hello()
def idOfSentenceJustSeen = 87
System.in.eachLine() { line ->  
 def argx = line.split(':')

if ((argx[0]) == '10') {
 String sentence = argx[1]
 h.step11(sentence)
}

if ((argx[0]) == '11') {
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

if ((argx[0]) == '12') {
 println "12: select S where l='has-root-poder' {label} //or 'has-word-cumpleanos' "
 String label = argx[1]
 h.step12(label)
}

if ((argx[0]) == '13') {
 println "13: insert l values hl {hyphenated label} //has-word-cumpleanos,has-root-frasco"
 String label = argx[1]
 h.step13(label)
}

if ((argx[0]) == '15') {
 println "15: select L where s{}"
 h.step15(idOfSentenceJustSeen)
}

if ((argx[0]) == '24') {
 println "24: select S where s limit 1"
 def temp = h.step24(idOfSentenceJustSeen)
 //temp == -1: no sentence found
 //temp  >  0: exactly one sentence found
 if (temp > 0) {
  idOfSentenceJustSeen = temp
  println "Just updated your current sentence"
 }
}

if ((argx[0]) == '16') {
 println "16: select S where s"
 h.step16(idOfSentenceJustSeen)
}

if ((argx[0]) == '17') {
 println "17: select S where s and r {regexp}"
 String regexp = argx[1]
 h.step17(idOfSentenceJustSeen, regexp)
}

if ((argx[0]) =~ '18') {
 println "18: select S where s and r limit 1 {regexp}"
 if (argx.size() > 1) {
 String regexp = argx[1]
 boolean printSQL = (argx[0] =~ 'q')
 def temp = h.step18(printSQL, idOfSentenceJustSeen, regexp)
 //temp == -1: no sentence found
 //temp  >  0: exactly one sentence found
 if (temp > 0) {
  idOfSentenceJustSeen = temp
 }
 }
 else println "?"
}

if ((argx[0]) == '19') {
 println "19: select S where r"
 String regexp = argx[1]
 h.step19(regexp)
}

if ((argx[0]) == '20') {
 println "20: select S where r limit 1"
 String regexp = argx[1]
 def temp = h.step20(regexp)
 //temp == -1: no sentence found
 //temp  >  0: exactly one sentence found
 if (temp > 0) {
  idOfSentenceJustSeen = temp
  println "Just updated your current sentence"
 }
}

if ((argx[0]) == '21') {
 println "21: select s where s"
 h.step21(idOfSentenceJustSeen)
}

if ((argx[0]) == '22') {
 println "22: insert values(s, l) {label}"
 String label = argx[1]
 h.step22(idOfSentenceJustSeen, label)
}

if ((argx[0]) == '23') {
 println "23: delete values(l) from s {label}"
 String label = argx[1]
 h.step23(idOfSentenceJustSeen, label)
}

if ((argx[0]) == '0') {
 println "10: insert s {sentence}"
 println "11: insert sample sentences and insert has-word labels (it)"
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
} 
}
/*
s/m: Use plagiarism algorithm
*/
