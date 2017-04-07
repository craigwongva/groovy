package upload

import groovy.json.*

import com.amazonaws.auth.*
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.s3.*;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3Client.*;

//To compile this code:
// /home/ec2-user/.sdkman/candidates/groovy/2.4.7/bin/groovyc -cp aws-java-sdk-1.10.50.jar upload.groovy
//To run this code:
// JARS1=.:./groovy-2.4.7.jar:./groovy-json-2.4.7.jar
// JARS2=aws-java-sdk-1.10.50.jar
// JARS3=commons-codec-1.6.jar:commons-logging-1.1.3.jar
// JARS4=httpclient-4.3.6.jar:httpcore-4.3.3.jar
// JARS5=jackson-annotations-2.5.0.jar:jackson-core-2.5.3.jar:jackson-databind-2.5.3.jar
// JARS6=joda-time-2.8.1.jar
// JARS=$JARS1:$JARS2:$JARS3:$JARS4:$JARS5:$JARS6
// java -cp $JARS \
//  -Daws.accessKeyId=REDACTED -Daws.secretKey=REDACTED \
//   upload/upload

//This code is borrowed from
//  http://docs.aws.amazon.com/AmazonS3/latest/dev/UploadObjSingleOpJava.html

//Here is the CLI command that is somewhat equivalent
// to the following code:
//  aws s3 cp craigdeleteme s3://s3-encryption-dgr
//   --region us-east-1 --sse-kms-key-id alias/craigkey --sse aws:kms
public class UploadObjectSingleOperation {

    def upload() {
	//Use BasicAWSCredentials (if you're going to hardcode the password (yuck)):
	// BasicAWSCredentials awsCreds = new BasicAWSCredentials("AKIAREDACTED ", "REDACTED")

	//Use SystemPropertiesCredentialsProvider (if you're going to use -D command line parms):
	def awsCreds = new SystemPropertiesCredentialsProvider().getCredentials() 

	Region usEast1 = Region.getRegion(Regions.US_EAST_1);
	def bucketName     = "s3-encryption-dgr";
	def keyName        = "craigdeleteme221"; //the target object name in S3
	def uploadFileName = "craigdeleteme222"; //the source file name in EC2 instance
	def keyID          = "alias/craigkey"    //aws kms create-key --description craig --region us-east-1
						 //aws kms create-alias --alias-name alias/craigkey 
						 // --target-key-id <value> --region us-east-1

        def s3client = new AmazonS3Client(awsCreds)
	s3client.setRegion(usEast1);

        try {
            println "Uploading a new object to S3 from a file\n"
            File file = new File(uploadFileName)
	    def por = new PutObjectRequest(bucketName, keyName, file)
		.withSSEAwsKeyManagementParams(new SSEAwsKeyManagementParams(keyID));
            s3client.putObject(por)

         } catch (Exception ase) {
            println "Caught an AmazonServiceException, which " +
            		"means your request made it " +
                    "to Amazon S3, but was rejected with an error response" +
                    " for some reason.";
	    println ase
            println "Error Message:    " + ase.getMessage()
            println "HTTP Status Code: " + ase.getStatusCode()
            println "AWS Error Code:   " + ase.getErrorCode()
            println "Error Type:       " + ase.getErrorType()
            println "Request ID:       " + ase.getRequestId()
        } catch (Exception ace) {
            println "Caught an AmazonClientException, which " +
            		"means the client encountered " +
                    "an internal error while trying to " +
                    "communicate with S3, " +
                    "such as not being able to access the network."
            println "Error Message: " + ace.getMessage()
        }
    }
}

def u = new UploadObjectSingleOperation()
u.upload()
