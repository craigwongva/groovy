package example

import groovy.json.*
import groovyx.net.http.*

import groovy.io.FileType

import java.sql.*

class Human {

 void foo(inputfilename) {
  Class.forName("org.h2.Driver");
  Connection conn = DriverManager.
  getConnection("jdbc:h2:~/cat", "sa", "");

  def stmt2 = conn.createStatement()
  try {
   String sql2 = "drop TABLE CAT.PUBLIC.HUMAN"
   def x2 = stmt2.execute(sql2)
  }
  catch (e) {
  }

  def stmt3 = conn.createStatement()
  String sql3 = '' +
   'create table cat.public.human (' +
   ' region varchar(100),' +
   ' instanceid varchar(100),' +
   ' xtimestamp varchar(100),' + 
   ' savepreviousxtimestamp varchar(100),' + 
   ' xtimestampSecondsInto2017 varchar(100),' +
   ' xyear int,' +
   ' xmonth int,' +
   ' xday int,' +
   ' xhour int,' +
   ' xminute int,' +
   ' xsecond int,' +
   ' xsavepreviousxtimestampSecondsInto2017 int,' +
   ' secondsPassed int,' +
   ' type varchar(100),' +
   ' secgrp varchar(100),' +
   ' status varchar(100),' +
   ' costperthisline decimal)' +
   " as select * from CSVREAD('deleteme4')"
   .replaceAll('deleteme4',inputfilename)

   def x3 = stmt3.execute(sql3)

  conn.close();
 }

}

def h = new Human()
h.foo(args[0])
