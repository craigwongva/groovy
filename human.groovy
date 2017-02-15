package example

import java.sql.*

/**
* Usage:
*  <this file> 20150217-0700-step2.csv
* where the csv file is the csv with derived costs per line
*/

class Human {

 void upload(inputfilename) {

  /**
  * cat is an arbitrary name
  */
  Class.forName("org.h2.Driver");
  Connection conn = DriverManager.

  // Use the default username/password:
  //  - the data is not sensitive
  //  - the h2 console is restricted by IP
  getConnection("jdbc:h2:tcp://localhost/~/cat", "sa", "");

  /**
  * Only one table is used for ad hoc analysis
  */
  def stmt2 = conn.createStatement()
  try {
   String sql2 = "drop TABLE CAT.PUBLIC.HUMAN"
   def x2 = stmt2.execute(sql2)
  }
  catch (e) {
  }

  /**
  * Use h2's CSVREAD
  */
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
h.upload(args[0])
