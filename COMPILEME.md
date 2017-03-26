/home/ec2-user/.sdkman/candidates/groovy/2.4.7/bin/groovyc -cp groovy-2.4.7.jar describe.groovy

/usr/bin/java -cp .:./groovy-2.4.7.jar example/describe &

--

cd /home/ec2-user/groovy

./userdata-h2

http://35.36.37.38:8082

--

/home/ec2-user/.sdkman/candidates/groovy/2.4.7/bin/groovyc -cp groovy-2.4.7.jar:h2/bin/h2-1.4.193.jar analyze.groovy

./steps123 20170216-0910

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

/home/ec2-user/.sdkman/candidates/groovy/2.4.7/bin/groovyc -cp groovy-2.4.7.jar:h2/bin/h2-1.4.193.jar dinh.groovy

JARPATH=".:./groovy-json-2.4.7.jar:./groovy-2.4.7.jar:h2/bin/h2-1.4.193.jar"
ANALYZECMD="/usr/bin/java -cp $JARPATH example/dinh"
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
aws s3 --profile=payer sync s3://radiantblue-billing/ .
unzip 398274688464-aws-billing-detailed-line-items-with-resources-and-tags-2017-02.csv.zip
#The following 'head' statement isn't done anymore, but I'm not sure how the dinh steps3 and steps5 know to ignore the last 13 (or so) lines.
head -n -13 398274688464-aws-billing-detailed-line-items-with-resources-and-tags-2017-02.csv > suspicious2
#edit the dinh.groovy to read the correct file 
/home/ec2-user/.sdkman/candidates/groovy/2.4.7/bin/groovyc -cp groovy-2.4.7.jar:h2/bin/h2-1.4.193.jar dinh.groovy
/usr/bin/java -cp .:./groovy-json-2.4.7.jar:./groovy-2.4.7.jar:h2/bin/h2-1.4.193.jar example/dinh step3 03    5 #from Amazon ...03.csv, create tables dinhraw5 and dinh5
/usr/bin/java -cp .:./groovy-json-2.4.7.jar:./groovy-2.4.7.jar:h2/bin/h2-1.4.193.jar example/dinh step4         #./dinhstep4
/usr/bin/java -cp .:./groovy-json-2.4.7.jar:./groovy-2.4.7.jar:h2/bin/h2-1.4.193.jar example/dinh step5 dummy 5 #update table dinh5

-- move to Herndon --

rm officeMoveToHerndonSecurityGroupsAllRegions

/usr/bin/java -cp .:./groovy-json-2.4.7.jar:./groovy-2.4.7.jar:h2/bin/h2-1.4.193.jar example/analyze analyzeOfficeMoveToHerndonStep1_FlattenDescribeInstances us-east-1 >> officeMoveToHerndonSecurityGroupsAllRegions
/usr/bin/java -cp .:./groovy-json-2.4.7.jar:./groovy-2.4.7.jar:h2/bin/h2-1.4.193.jar example/analyze analyzeOfficeMoveToHerndonStep1_FlattenDescribeInstances us-west-2 >> officeMoveToHerndonSecurityGroupsAllRegions
/usr/bin/java -cp .:./groovy-json-2.4.7.jar:./groovy-2.4.7.jar:h2/bin/h2-1.4.193.jar example/analyze analyzeOfficeMoveToHerndonStep1_FlattenDescribeInstances us-west-1 >> officeMoveToHerndonSecurityGroupsAllRegions

aws ec2 describe-security-groups --region us-west-1 > officeMoveToHerndonSecurityGroupsUswest1
aws ec2 describe-security-groups --region us-west-2 > officeMoveToHerndonSecurityGroupsUswest2
aws ec2 describe-security-groups --region us-east-1 > officeMoveToHerndonSecurityGroupsUsEast1

/home/ec2-user/.sdkman/candidates/groovy/2.4.7/bin/groovyc -cp groovy-2.4.7.jar:h2/bin/h2-1.4.193.jar analyze.groovy

/usr/bin/java -cp .:./groovy-json-2.4.7.jar:./groovy-2.4.7.jar:h2/bin/h2-1.4.193.jar example/analyze analyzeOfficeMoveToHerndonStep2_FlattenDescribeSecurityGroups officeMoveToHerndonSecurityGroupsUseast1 > deleteme9useast1

/usr/bin/java -cp .:./groovy-json-2.4.7.jar:./groovy-2.4.7.jar:h2/bin/h2-1.4.193.jar example/analyze analyzeOfficeMoveToHerndonStep3_Upload deleteme9useast1

