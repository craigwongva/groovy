def process = [ "curl", "-s", "http://localhost:8888" ].execute()
process.waitFor()
def ptext = process.text

if (ptext =~ 'status green') {
 String msg = 
  ptext[
   ptext.indexOf('captured')..
   ptext.indexOf('json')+'son'.size() ]
 def x = "python ses.py $msg".execute().waitFor()
}
else {
}
