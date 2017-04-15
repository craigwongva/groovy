import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.simpleemail.*
import com.amazonaws.services.simpleemail.model.*

Region usWest2 = Region.getRegion(Regions.US_WEST_2)
AmazonSimpleEmailService client = new AmazonSimpleEmailServiceClient();
client.setRegion(usWest2);

while (1 > 0) {

def env = System.getenv()
String monitorIP = env['MONITOR_IP']
String monitorEmail = env['MONITOR_EMAIL']

def process = [ "curl", "-s", monitorIP ].execute()
process.waitFor()
def ptext = process.text

if (!(ptext =~ 'status green')) {
 String msg 
 if (ptext == '') {
  msg = 'The monitor is not running!'
 }
 else { 
  msg =
  ptext[
   ptext.indexOf('captured')..
   ptext.indexOf('json')+'son'.size() ]
 }
 sendEmail(client, monitorEmail, 'latest snapshot', msg)
}

 sleep 1000*(30*60)
}

def sendEmail(client, String monitorEmail, String subject, String body) {
//http://docs.aws.amazon.com/AWSJavaSDK/latest/
// javadoc/com/amazonaws/services/simpleemail/
// AmazonSimpleEmailServiceClient.html#
// sendEmail-
// com.amazonaws.services.simpleemail.model.SendEmailRequest-
SendEmailRequest request = new SendEmailRequest()
 .withDestination(
  new Destination()
   .withToAddresses(monitorEmail))
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
       .withSource("EC2 Snapshot <${monitorEmail}>")

SendEmailResult response = client.sendEmail(request)
}
