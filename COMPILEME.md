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

unzip 398274688464-aws-billing-detailed-line-items-with-resources-and-tags-2017-02.csv.zip
head -n -13 398274688464-aws-billing-detailed-line-items-with-resources-and-tags-2017-02.csv > suspicious2
/usr/bin/java -cp .:./groovy-json-2.4.7.jar:./groovy-2.4.7.jar:h2/bin/h2-1.4.193.jar example/dinh step3
