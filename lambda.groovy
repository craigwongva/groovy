import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSClient;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;

import com.amazonaws.services.route53.AmazonRoute53Client;
import com.amazonaws.services.route53.model.*;

Region usWest2 = Region.getRegion(Regions.US_WEST_2);

def ec2Client = new com.amazonaws.services.ec2.AmazonEC2Client()
ec2Client.setRegion(usWest2);

DescribeInstancesRequest request = new DescribeInstancesRequest();

List<String> valuesT1 = new ArrayList<String>();
valuesT1.add("trac");
Filter filter1 = new Filter("tag:Name", valuesT1);

DescribeInstancesResult result = 
 ec2Client.describeInstances(request.withFilters(filter1));
List<Reservation> reservations = result.getReservations();

String ec2wPublicIpAddress
for (Reservation reservation : reservations) {
 List<Instance> instances = reservation.getInstances();
 for (Instance instance : instances) {
  ec2wPublicIpAddress = instance.getPublicIpAddress();
 }
}


def route53Client = new com.amazonaws.services.route53.AmazonRoute53Client()

def hostedZoneId = 'Z14MX2BF8JTF7J'
def lrrsReq = new ListResourceRecordSetsRequest(hostedZoneId)
def lrrsRes = route53Client.listResourceRecordSets(lrrsReq)
def lrrs = lrrsRes.getResourceRecordSets()
ResourceRecordSet myrrs
lrrs.grep{it.getName() == 'test.redf4rth.net.'}.each {
  myrrs = it
}
def z = new com.amazonaws.internal.SdkInternalList()
def myrrs2 = myrrs.getResourceRecords()
myrrs2.each {
 it.setValue(ec2wPublicIpAddress)
 z.add(it)
}
myrrs.setResourceRecords(z)



/* 
   I couldn't build a homegrown ResourceRecordSet.
   So I'm reading and updating the existing one. 
*/

def c = new Change('UPSERT', myrrs)
def changes = [] 
changes.add(c)
def cb = new ChangeBatch()
cb.setChanges(changes)

def crrsReq = 
 new ChangeResourceRecordSetsRequest(hostedZoneId, cb)
def crrsRes = 
 route53Client.changeResourceRecordSets(crrsReq)
