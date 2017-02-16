/home/ec2-user/.sdkman/candidates/groovy/2.4.7/bin/groovyc -cp groovy-2.4.7.jar describe.groovy

/usr/bin/java -cp .:./groovy-2.4.7.jar example/describe

--

/home/ec2-user/.sdkman/candidates/groovy/2.4.7/bin/groovyc -cp groovy-2.4.7.jar analyze.groovy

/usr/bin/java -cp .:./groovy-json-2.4.7.jar:./groovy-2.4.7.jar example/analyze > derived/20170213-1420-step1.csv

--


cd /home/ec2-user/groovy

./userdata-h2

http://35.36.37.38:8082

--

#The following aren't optimized, i.e. there might be unnecessary jars included.

/home/ec2-user/.sdkman/candidates/groovy/2.4.7/bin/groovyc -cp groovy-2.4.7.jar:h2/bin/h2-1.4.193.jar relational.groovy

/usr/bin/java -cp .:./groovy-json-2.4.7.jar:./groovy-2.4.7.jar:h2/bin/h2-1.4.193.jar example/relational derived/20170213-1420-step1.csv > derived/20170213-1420-step2.csv

aws s3 cp derived/20170213-1420-step2.csv s3://venicegeo-devops-dev-analyze-project/

--

/home/ec2-user/.sdkman/candidates/groovy/2.4.7/bin/groovyc -cp groovy-2.4.7.jar:h2/bin/h2-1.4.193.jar human.groovy

/usr/bin/java -cp .:./groovy-json-2.4.7.jar:./groovy-2.4.7.jar:h2/bin/h2-1.4.193.jar example/human derived/20170213-1420-step2.csv


-- -- -- --

/home/ec2-user/.sdkman/candidates/groovy/2.4.7/bin/groovyc -cp groovy-2.4.7.jar:h2/bin/h2-1.4.193.jar analyze.groovy

/usr/bin/java -cp .:./groovy-json-2.4.7.jar:./groovy-2.4.7.jar:h2/bin/h2-1.4.193.jar example/analyze step1 > derived/20170216-0800-step1.csv
