import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSClient;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;

Region usWest2 = Region.getRegion(Regions.US_WEST_2);

def ec2Client = new com.amazonaws.services.ec2.AmazonEC2Client()
ec2Client.setRegion(usWest2);

DescribeInstancesRequest request = new DescribeInstancesRequest();

List<String> valuesT1 = new ArrayList<String>();
valuesT1.add("trac");
Filter filter1 = new Filter("tag:Name", valuesT1);

DescribeInstancesResult result = ec2Client.describeInstances(request.withFilters(filter1));
List<Reservation> reservations = result.getReservations();

for (Reservation reservation : reservations) {
List<Instance> instances = reservation.getInstances();
for (Instance instance : instances) {
System.out.println(instance.getInstanceId());
}
}

/* The following code successfully sees an SQS queue,
   but the code can be found elsewhere:
   -b glo redf4rth-grails

String QUEUE1 = 'BIG' //"groovylambda1"
def sqsClient = new com.amazonaws.services.sqs.AmazonSQSClient()
sqsClient.setRegion(usWest2);
String temp1 = sqsClient.getQueueUrl(QUEUE1)
println temp1
def x = sqsClient.listQueues()
def y = x.getQueueUrls()
println y.getClass()
println y[0].getClass()
println y[0]
//https://sqs.us-west-2.amazonaws.com/994238729631/groovylambda1
*/

//aws ec2 describe-instances --filter Name=tag:Name,Values=trac --query Reservations[].Instances[].[PublicIpAddress] --region us-west-2 --output text
