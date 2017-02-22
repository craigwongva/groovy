//package example

import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.simpleemail.*
import com.amazonaws.services.simpleemail.model.*

Region usWest2 = Region.getRegion(Regions.US_WEST_2)
AmazonSimpleEmailService client = new AmazonSimpleEmailServiceClient();
client.setRegion(usWest2);

//http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/simpleemail/AmazonSimpleEmailServiceClient.html#sendEmail-com.amazonaws.services.simpleemail.model.SendEmailRequest-
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
         .withData("This is the message body in text format.")))
     .withSubject(
      new Content()
       .withCharset("UTF-8")
       .withData("Test email")))
       .withSource("craigjk@cox.net")

SendEmailResult response = client.sendEmail(request);
