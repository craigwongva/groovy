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
/* this works:
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

String upsertJson = "{\"Comment\":\"\",\"Changes\":[{\"Action\":\"UPSERT\",\"ResourceRecordSet\":{\"Name\":\"trac.redf4rth.net\",\"Type\":\"A\",\"TTL\":300,\"ResourceRecords\":[{\"Value\":\"$ec2wPublicIpAddress\"}]}}]}" 
println upsertJson
*/
def route53Client = new com.amazonaws.services.route53.AmazonRoute53Client()

println "Reading from Route 53 now..."

def hostedZoneId = 'Z14MX2BF8JTF7J'
def lrrsReq = new ListResourceRecordSetsRequest(hostedZoneId)
def lrrsRes = route53Client.listResourceRecordSets(lrrsReq)
def lrrs = lrrsRes.getResourceRecordSets()
ResourceRecordSet myrrs
lrrs.each {
 if (it.getName() == 'earn.redf4rth.net.') {
  println "I see test: " + it.getClass() + ' ' + it.getResourceRecords()
  myrrs = it
 }
}
println "myrrs is a " + myrrs.getClass()
def myrrs2 = myrrs.getResourceRecords()
println "myrrs2 is a " + myrrs2.getClass()
myrrs2.each {
 print it 
 print  ' and Amazon is a ' 
 println it.getClass()
}


println "Assembling my homegrown now..."

def ec2wPublicIpAddress = '[33.44.55.66]'
def rr = new ResourceRecord(ec2wPublicIpAddress)
com.amazonaws.internal.SdkInternalList resourceRecords = new com.amazonaws.internal.SdkInternalList()
resourceRecords.add(rr)
println "resourceRecords is a " + resourceRecords.getClass()
resourceRecords.each {
 print it
 print  ' AND Craig IS A '
 println it.getClass()
}
def rrs = new ResourceRecordSet('earn.redf4rth.net.', 'A')
rrs.setResourceRecords(resourceRecords)


def c = new Change('UPSERT', myrrs)
def changes = [] //List<Change>
changes.add(c)
def cb = new ChangeBatch()
cb.setChanges(changes)

//def hostedZoneId = 'Z14MX2BF8JTF7J'
def crrsReq = 
 new ChangeResourceRecordSetsRequest(hostedZoneId, cb)
def crrsRes = 
 route53Client.changeResourceRecordSets(crrsReq)


/*
sudo -u ec2-user bash -c 'aws route53 change-resource-record-sets --hosted-zone-id Z14MX2BF8JTF7J --change-batch file://upsert-route53.json.tmp'
*/
