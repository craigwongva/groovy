/home/ec2-user/.sdkman/candidates/groovy/2.4.7/bin/groovyc -cp groovy-2.4.7.jar describe.groovy

/usr/bin/java -cp .:./groovy-2.4.7.jar example/describe &

--

cd /home/ec2-user/groovy

./userdata-h2

http://35.36.37.38:8082

--

/home/ec2-user/.sdkman/candidates/groovy/2.4.7/bin/groovyc -cp groovy-2.4.7.jar:h2/bin/h2-1.4.193.jar blue.groovy

./blue-steps123 20170216-0910

java -jar h2/bin/h2-1.4.193.jar -webAllowOthers &

--

aws s3 cp derived/20170223-0720-step2.csv s3://venicegeo-devops-dev-analyze-project/

--

/home/ec2-user/.sdkman/candidates/groovy/2.4.7/bin/groovy -l 8888 monitor.groovy &

--

#/home/ec2-user/.sdkman/candidates/groovy/2.4.7/bin/groovyc -cp groovy-2.4.7.jar:aws-java-sdk-1.10.50.jar ses.groovy

#/usr/bin/java -cp .:./groovy-2.4.7.jar:aws-java-sdk-1.10.50.jar:commons-codec-1.6.jar:commons-logging-1.1.3.jar:httpclient-4.3.6.jar:httpcore-4.3.3.jar:jackson-annotations-2.5.0.jar:jackson-core-2.5.3.jar:jackson-databind-2.5.3.jar:joda-time-2.8.1.jar ses

/home/ec2-user/.sdkman/candidates/groovy/2.4.7/bin/groovy -cp .:./groovy-2.4.7.jar:aws-java-sdk-1.10.50.jar:commons-codec-1.6.jar:commons-logging-1.1.3.jar:httpclient-4.3.6.jar:httpcore-4.3.3.jar:jackson-annotations-2.5.0.jar:jackson-core-2.5.3.jar:jackson-databind-2.5.3.jar:joda-time-2.8.1.jar groovyses.groovy &

--

/home/ec2-user/.sdkman/candidates/groovy/2.4.7/bin/groovyc -cp groovy-2.4.7.jar:h2/bin/h2-1.4.193.jar orange.groovy

JARPATH=".:./groovy-json-2.4.7.jar:./groovy-2.4.7.jar:h2/bin/h2-1.4.193.jar"
ANALYZECMD="/usr/bin/java -cp $JARPATH example/orange"
$ANALYZECMD step3

--

export MONITOR_IP=`curl http://169.254.169.254/latest/meta-data/public-ipv4`:8888
export MONITOR_EMAIL=
describe &
monitor &
groovyses &

--

cd .aws
#populate config and credentials files
#via the Console, activate the user's access key
aws s3 --profile=payer sync s3://radiantblue-billing/ .
#expect to enter the user's MFA
#via the Console, de-activate the user's access key
#create a directory so .aws doesn't get cluttered, e.g. mkdir radiantblue-billing-20170418
#cd to your new directory
unzip 398274688464-aws-billing-detailed-line-items-with-resources-and-tags-2017-02.csv.zip
#copy the unzipped file into ~/groovy, as a convenience
#The following 'head' statement isn't done anymore, but I'm not sure how the orange steps3 and steps5 know to ignore the last 13 (or so) lines.
head -n -13 398274688464-aws-billing-detailed-line-items-with-resources-and-tags-2017-02.csv > suspicious2
#edit the orange.groovy to read the correct file 
/home/ec2-user/.sdkman/candidates/groovy/2.4.7/bin/groovyc -cp groovy-2.4.7.jar:h2/bin/h2-1.4.193.jar orange.groovy
/usr/bin/java -cp .:./groovy-json-2.4.7.jar:./groovy-2.4.7.jar:h2/bin/h2-1.4.193.jar example/orange step1 03    5 #from Amazon ...03.csv, create tables orangeraw5 and orange5
#In April, the above took 3m33s.
/usr/bin/java -cp .:./groovy-json-2.4.7.jar:./groovy-2.4.7.jar:h2/bin/h2-1.4.193.jar example/orange step2         #./orangestep2
#In April, the above took less than 5s.
/usr/bin/java -cp .:./groovy-json-2.4.7.jar:./groovy-2.4.7.jar:h2/bin/h2-1.4.193.jar example/orange step3 dummy 5 #update table orange5
#In April, the above took about 25s.
#run the queries in file 'orangeAnalysisQueries' (sha 1697ed1) but be sure to change the orange_dinh# table# to get current month results

-- move to Herndon --

---- describe-instances

rm officeMoveToHerndonSecurityGroupsAllRegions

/usr/bin/java -cp .:./groovy-json-2.4.7.jar:./groovy-2.4.7.jar:h2/bin/h2-1.4.193.jar example/blue analyzeOfficeMoveToHerndonStep1_FlattenDescribeInstances us-east-1 >> officeMoveToHerndonSecurityGroupsAllRegions
/usr/bin/java -cp .:./groovy-json-2.4.7.jar:./groovy-2.4.7.jar:h2/bin/h2-1.4.193.jar example/blue analyzeOfficeMoveToHerndonStep1_FlattenDescribeInstances us-west-2 >> officeMoveToHerndonSecurityGroupsAllRegions
/usr/bin/java -cp .:./groovy-json-2.4.7.jar:./groovy-2.4.7.jar:h2/bin/h2-1.4.193.jar example/blue analyzeOfficeMoveToHerndonStep1_FlattenDescribeInstances us-west-1 >> officeMoveToHerndonSecurityGroupsAllRegions

/usr/bin/java -cp .:./groovy-json-2.4.7.jar:./groovy-2.4.7.jar:h2/bin/h2-1.4.193.jar example/blue analyzeOfficeMoveToHerndonStep1a_Upload officeMoveToHerndonSecurityGroupsAllRegions

---- 
aws ec2 describe-security-groups --region us-west-1 > officeMoveToHerndonSecurityGroupsUswest1
aws ec2 describe-security-groups --region us-west-2 > officeMoveToHerndonSecurityGroupsUswest2
aws ec2 describe-security-groups --region us-east-1 > officeMoveToHerndonSecurityGroupsUseast1

/home/ec2-user/.sdkman/candidates/groovy/2.4.7/bin/groovyc -cp groovy-2.4.7.jar:h2/bin/h2-1.4.193.jar blue.groovy

/usr/bin/java -cp .:./groovy-json-2.4.7.jar:./groovy-2.4.7.jar:h2/bin/h2-1.4.193.jar example/blue analyzeOfficeMoveToHerndonStep2_FlattenDescribeSecurityGroups officeMoveToHerndonSecurityGroupsUswest1 > deleteme9uswest1
/usr/bin/java -cp .:./groovy-json-2.4.7.jar:./groovy-2.4.7.jar:h2/bin/h2-1.4.193.jar example/blue analyzeOfficeMoveToHerndonStep2_FlattenDescribeSecurityGroups officeMoveToHerndonSecurityGroupsUswest2 > deleteme9uswest2
/usr/bin/java -cp .:./groovy-json-2.4.7.jar:./groovy-2.4.7.jar:h2/bin/h2-1.4.193.jar example/blue analyzeOfficeMoveToHerndonStep2_FlattenDescribeSecurityGroups officeMoveToHerndonSecurityGroupsUseast1 > deleteme9useast1

rm deleteme9allregions

cat deleteme9uswest1 deleteme9uswest2 deleteme9useast1 > deleteme9allregions

/usr/bin/java -cp .:./groovy-json-2.4.7.jar:./groovy-2.4.7.jar:h2/bin/h2-1.4.193.jar example/blue analyzeOfficeMoveToHerndonStep3_Upload deleteme9allregions

-- Which networkACLs mention the RB IP address?
aws ec2 describe-network-acls --region us-east-1 > network-acls-useast1

/home/ec2-user/.sdkman/candidates/groovy/2.4.7/bin/groovyc -cp groovy-2.4.7.jar:h2/bin/h2-1.4.193.jar blue.groovy

#It's not necessary to upload this into h2,
# because there's nothing interesting to join a network ACL against anyway.
/usr/bin/java -cp .:./groovy-json-2.4.7.jar:./groovy-2.4.7.jar:h2/bin/h2-1.4.193.jar example/blue analyzeOfficeMoveToHerndonStep1_FlattenDescribeNetworkAcls network-acls-useast1 | grep 207

#run the herndonAnalysisQueries

-- Tag instances any time of the month (it's manual right now):

/home/ec2-user/.sdkman/candidates/groovy/2.4.7/bin/groovyc -cp groovy-2.4.7.jar:h2/bin/h2-1.4.193.jar tagger.groovy

/usr/bin/java -cp .:./groovy-json-2.4.7.jar:./groovy-2.4.7.jar:h2/bin/h2-1.4.193.jar tag/tagger

Look at the output that suggests tags needed.  
Remove the --dry-run.  
Run the commands to tag the instances. For example:  
   aws ec2 create-tags --resources i-0457cae63f023ed9a i-09daee605405d6bc6 --tags Key=Project,Value=geowave-dev --region us-east-1  
   aws ec2 create-tags --resources i-07014cdcf56b0d018 --tags Key=Project,Value=piazza-dev --region us-east-1  
   aws ec2 create-tags --resources i-00d5ed2271af2ae19 --tags Key=Project,Value=mrgeo-dev --region us-east-1  

