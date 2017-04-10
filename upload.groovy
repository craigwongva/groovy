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

    def createFiveKeys() {

        def spaceEncryptionkey = [
      	    'test': '9d756117-08ce-4426-9f88-160576b6325e,piazza-kms-test,gsn-iam-test-S3BlobstoreUser-14JQDCWOCNXRU',
            'dev':  '90a294e3-13c0-47ba-bf08-6f88f286de84,piazza-kms-dev,gsn-iam-dev-S3BlobstoreUser-1OM0QYZL8BTEP',
            'int':  '42de4feb-844f-43d3-bff9-6bde53474aff,piazza-kms-int,gsn-iam-int-S3BlobstoreUser-W4XRSOXFO9VF',
            'stage':'bcdebfc2-68d1-4473-8675-73da960326e1,piazza-kms-stage,gsn-iam-stage-S3BlobstoreUser-MIF4H60PGOSX',
            'prod': '02291128-c6ab-46bf-a918-c989fb6e2c71,piazza-kms-prod,gsp-iam-prod-S3BlobstoreUser-1056HWJ5M3PXS'
        ]
    
        spaceEncryptionkey.each { k, v ->
            def keyId = v.split(',')[0]
            def alias = v.split(',')[1]
            def userid = v.split(',')[2]
           
            //AWS KMS enforces a 7 day waiting period before deletion, so
            // the next five lines are commented out to prevent creation of new keys.
            //def s1 = "aws kms create-key --description $alias --region us-east-1 --output text".execute()
            //def t1 = s1.text.split()
            //def keyId = t1[6] 
            
            println "I just pretend created $keyId"
            
            def s2 = "aws kms create-alias --alias-name alias/$alias --target-key-id $keyId --region us-east-1".execute()
            println ""
            println "create-alias err.text: ${s2.err.text}"
            println "create-alias    .text: ${s2.text}"
            
            def s3 = 'aws kms put-key-policy --key-id ' + keyId + ' --region us-east-1 --policy-name default --policy \'{"Version":"2012-10-17","Id":"key-consolepolicy-3","Statement":[{"Sid":"EnableIAMUserPermissions","Effect":"Allow","Principal":{"AWS":"arn:aws:iam::539674021708:root"},"Action":"kms:*","Resource":"*"},{"Sid":"AllowAccessForKeyAdministrators","Effect":"Allow","Principal":{"AWS":"arn:aws:iam::539674021708:user/cwong"},"Action":["kms:Create*","kms:Describe*","kms:Enable*","kms:List*","kms:Put*","kms:Update*","kms:Revoke*","kms:Disable*","kms:Get*","kms:Delete*","kms:TagResource","kms:UntagResource","kms:ScheduleKeyDeletion","kms:CancelKeyDeletion"],"Resource":"*"},{"Sid":"AllowUseOfTheKey","Effect":"Allow","Principal":{"AWS":["arn:aws:iam::539674021708:user/' + userid + '"]},"Action":["kms:Encrypt","kms:Decrypt","kms:ReEncrypt*","kms:GenerateDataKey*","kms:DescribeKey"],"Resource":"*"},{"Sid":"AllowAttachmentOfPersistentResources","Effect":"Allow","Principal":{"AWS":["arn:aws:iam::539674021708:user/' + userid + '"]},"Action":["kms:CreateGrant","kms:ListGrants","kms:RevokeGrant"],"Resource":"*","Condition":{"Bool":{"kms:GrantIsForAWSResource":"true"}}}]}\' '
            
           	//This script isn't able to submit the put-key-policy command.
            // The problem is probably related to Groovy's handling of embedded
            // whitespace in .execute() command strings.
            println "\nRun the following manually from the command line for $k:\n"
            println s3
            println ""
        }
    }

    def upload() {
	//Use BasicAWSCredentials (if you're going to hardcode the password (yuck)):
	// BasicAWSCredentials awsCreds = new BasicAWSCredentials("AKIAREDACTED ", "REDACTED")

	//Use SystemPropertiesCredentialsProvider (if you're going to use -D command line parms):
	def awsCreds = new SystemPropertiesCredentialsProvider().getCredentials() 

	Region usEast1 = Region.getRegion(Regions.US_EAST_1);
	def bucketName     = "s3-encryption-dgr"
	def keyName        = "craigdeleteme221" //the target object name in S3
	def uploadFileName = "craigdeleteme222" //the source file name in EC2 instance
	def keyID          = "alias/piazza-kms-test"

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
//u.createFiveKeys()
u.upload()
