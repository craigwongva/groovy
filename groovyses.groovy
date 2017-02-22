import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.simpleemail.*
import com.amazonaws.services.simpleemail.model.*

Region usWest2 = Region.getRegion(Regions.US_WEST_2)
AmazonSimpleEmailService client = new AmazonSimpleEmailServiceClient();
client.setRegion(usWest2);


def process = [ "curl", "-s", "http://localhost:8888" ].execute()
process.waitFor()
def ptext = process.text

if (ptext =~ 'status green') {
 String msg = 
  ptext[
   ptext.indexOf('captured')..
   ptext.indexOf('json')+'son'.size() ]
 //def x = "python ses.py $msg".execute().waitFor()
 sendEmail(client, 'hello', msg)
 println "I sent an email"
}
else {
 println "No need to send an email"
}


def sendEmail(client, String subject, String body) {
//http://docs.aws.amazon.com/AWSJavaSDK/latest/
// javadoc/com/amazonaws/services/simpleemail/
// AmazonSimpleEmailServiceClient.html#
// sendEmail-
// com.amazonaws.services.simpleemail.model.SendEmailRequest-
SendEmailRequest request = new SendEmailRequest()
 .withDestination(
  new Destination()
   .withToAddresses("craigjk@cox.net"))
   .withMessage(
    new Message()
     .withBody(
      new Body()
       .withText(
        new Content()
         .withCharset("UTF-8")
         .withData(body)))
     .withSubject(
      new Content()
       .withCharset("UTF-8")
       .withData(subject)))
       .withSource("EC2 Snapshot <craigjk@cox.net>")

SendEmailResult response = client.sendEmail(request)
}
