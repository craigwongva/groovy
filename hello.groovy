import groovy.json.*

void interpretEC2jsonAndPrint(f, t) {
   //The interpreted json values are printed here as a convenience
   // rather than returning them within an array.
   def object = jsonSlurper.parseText(t)
   for (def i = 0; i < object.Reservations.size(); i++) {
   for (def j = 0; j < object.Reservations[i].Instances.size(); j++) {
    def instanceId   = object.Reservations[i].Instances[j].InstanceId
    def instanceType = object.Reservations[i].Instances[j].InstanceType
    def keyName      = object.Reservations[i].Instances[j].KeyName
    def state        = object.Reservations[i].Instances[j].State.Name
    def projectValue = getTagValue(object.Reservations[i].Instances[j].Tags, 'Project')
    def nameValue    = getTagValue(object.Reservations[i].Instances[j].Tags, 'Name')

    print   "${f.region},${f.timestamp},${f.timestampSecondsInto2017},"
    print   "${f.timestampYear},${f.timestampMonth},${f.timestampDay},"
    print   "${f.timestampHour},${f.timestampMinute},${f.timestampSecond},"
    println "$instanceId,$instanceType,$keyName,$state,$projectValue,$nameValue"
   }
   }
 }

 void interpretEC2DescribeSecurityGroupsAndPrint(String region, t) {
  def object = jsonSlurper.parseText(t)
   for (def i = 0; i < object.SecurityGroups.size(); i++) {
    def groupId = object.SecurityGroups[i].GroupId
    def groupName = object.SecurityGroups[i].GroupName
    def description = object.SecurityGroups[i].Description
    description = description.replaceAll(',', ' comma')
    def ipPermissions = object.SecurityGroups[i].IpPermissions
    for (def j = 0; j < object.SecurityGroups[i].IpPermissions.size(); j++) {
     def fromPort = object.SecurityGroups[i].IpPermissions[j].FromPort
     def ipRanges = object.SecurityGroups[i].IpPermissions[j].IpRanges
     for (def k = 0; k < ipRanges.size(); k++) {
      def cidrIp = ipRanges[k].CidrIp
      def shortDesc = (description.size() >= 12)? description[0..11]: description
      println "$region,$groupId,$groupName,$description,$fromPort,$cidrIp"
     }
    }
   }
 }

void interpretEC2jsonForTrueSecurityGroupsAndPrint(region, t) {
   //The interpreted json values are printed here as a convenience
   // rather than returning them within an array.
   def object = jsonSlurper.parseText(t)
   for (def i = 0; i < object.Reservations.size(); i++) {
    for (def j = 0; j < object.Reservations[i].Instances.size(); j++) {
     def instanceId     = object.Reservations[i].Instances[j].InstanceId
     def securityGroups = object.Reservations[i].Instances[j].SecurityGroups
     for (def k = 0; k < securityGroups.size(); k++) {
      def groupName = securityGroups[k].GroupName
      def groupId   = securityGroups[k].GroupId
      println "$region,$instanceId,$groupId,$groupName"
     }
    }
   }
 }

 void analyzeOfficeMoveToHerndonStep2_FlattenDescribeSecurityGroups(String inputfilename) {
   String region
   if (inputfilename =~ /east1/) region = 'us-east-1'
   if (inputfilename =~ /west1/) region = 'us-west-1'
   if (inputfilename =~ /west2/) region = 'us-west-2'
   String t = new File(inputfilename).text
   interpretEC2DescribeSecurityGroupsAndPrint(region, t)
 }

//println "bonjour!"
//[[IpProtocol:-1, IpRanges:[[CidrIp:0.0.0.0/0]], Ipv6Ranges:[], PrefixListIds:[], UserIdGroupPairs:[]]]
def s = """
{
    "SecurityGroups": [
        {
            "IpPermissionsEgress": [
                {
                    "IpProtocol": "-1",
                    "PrefixListIds": [],
                    "IpRanges": [
                        {
                            "CidrIp": "0.0.0.0/0"
                        }
                    ],
                    "UserIdGroupPairs": [],
                    "Ipv6Ranges": []
                }
            ],
            "Description": "saynext",
            "IpPermissions": [],
            "GroupName": "saynext",
            "VpcId": "vpc-a0383dc5",
            "OwnerId": "994238729631",
            "GroupId": "sg-f922ff86"
        }
    ]   
}
"""

   def jsonSlurper = new JsonSlurper()

   def object = jsonSlurper.parseText(s)
   def SecurityGroupsList = object.SecurityGroups
   //println "SecurityGroupsList size is ${SecurityGroupsList.size()}"
   def p = SecurityGroupsList[0].IpPermissionsEgress

def f = """
{
  "events": [
    {
      "ingestionTime": 1396035394997,
      "timestamp": 1396035378988,
      "message": "ERROR Event 1",
      "logStreamName": "my-log-stream-1",
      "eventId": "31132629274945519779805322857203735586714454643391594505"
    },
    {
      "ingestionTime": 1396035394997,
      "timestamp": 1396035378988,
      "message": "ERROR Event 2",
      "logStreamName": "my-log-stream-2",
      "eventId": "31132629274945519779805322857203735586814454643391594505"
    },
    {
      "ingestionTime": 1396035394997,
      "timestamp": 1396035378989,
      "message": "ERROR Event 3",
      "logStreamName": "my-log-stream-3",
      "eventId": "31132629274945519779805322857203735586824454643391594505"
    }
  ],
  "searchedLogStreams": [
    {
      "searchedCompletely": true, 
      "logStreamName": "my-log-stream-1"
    }, 
    {
      "searchedCompletely": true,      
      "logStreamName": "my-log-stream-2"
    },
    {
      "searchedCompletely": false,
      "logStreamName": "my-log-stream-3"
    }
  ],
  "nextToken": "ZNUEPl7FcQuXbIH4Swk9D9eFu2XBg-ijZIZlvzz4ea9zZRjw-MMtQtvcoMdmq4T29K7Q6Y1e_KvyfpcT_f_tUw"
}
"""

   //groovy: I'm unable to get a filter pattern with an embedded space to work:
   def w = "aws logs filter-log-events --log-group-name microcero-catalina --region us-east-1 --filter-pattern running"
   def x = w.execute().text

   def fObject = jsonSlurper.parseText(x) //(f)
   def events = fObject.events
   for (int i=0; i<events.size(); i++) {
       println events[i].message
   }
