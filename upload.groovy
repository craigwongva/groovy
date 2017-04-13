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
//
//To run this code (for the 'upload' method (to demo uploading+encrypting))::
// JARS1=.:./groovy-2.4.7.jar:./groovy-json-2.4.7.jar
// JARS2=aws-java-sdk-1.10.50.jar
// JARS3=commons-codec-1.6.jar:commons-logging-1.1.3.jar
// JARS4=httpclient-4.3.6.jar:httpcore-4.3.3.jar
// JARS5=jackson-annotations-2.5.0.jar:jackson-core-2.5.3.jar:jackson-databind-2.5.3.jar
// JARS6=joda-time-2.8.1.jar
// JARS=$JARS1:$JARS2:$JARS3:$JARS4:$JARS5:$JARS6 
//
//To run this code (for the createOneEncryptionKeyPerPCFInstance method):
// java -cp $JARS \
//   upload/upload createOneEncryptionKeyPerPCFInstance 
//    <buildKeys> <encryptionKeyId> <encryptionKeyDescription>
//where <buildKeys> is true or false
//If <buildKeys> is true, then <encryptionKeyId> is ignored
//Note 1: encryptionKeyDescription looks like 'x-y-l2'
//Note 2: AWS KMS enforces a 7 day waiting period before deletion, so
// in order to prevent an overabundance of test keys, set to false.
//
//To run this code (for the uups method):
// java -cp $JARS \
//   upload/upload <cfuser> <cfpassword> <cfspace> <encryptionKeyAlias>
//Note 1: encryptionKeyAlias must include the prefix 'alias/'
//Note 2: An empty list of apps is a problem because
// at least one app is needed to get the pz-blobstore
// credentials (because there is a 'cf cups' and
// 'cf uups' but no 'cf list-user-provided-service').
//
//To run this code (for the upload+encrypt demo):
// java -cp $JARS \
//  -Daws.accessKeyId=REDACTED -Daws.secretKey=REDACTED \
//   upload/upload
//Note 1: The two redacted values are passed in via
// -D parameters, not within args[]

public class UploadObjectSingleOperation {

    def uupsPzblobstore(args) {
	def (
	    ignore,		//name of method to invoke
            user, 		//PCF userid that can uups pz-blobstore
            pwd, 		//
            cfspace, 		//method only updates one space's pz-blobstore
            encryptionkey	//do include 'alias/', e.g. 'alias/zzz-zzz-zzz'
        ) = args

        String cfcmd = '/home/ec2-user/groovy/cf'

	cflogin(cfcmd, user, pwd, cfspace)

        ArrayList appsUsingBlobstore = getAppsUsingBlobstore(cfcmd)
	if (appsUsingBlobstore.size() == 0) {
	    println "Error: At least one app must use pz-blobstore."
	    return
	}

        getUpsCredentialsAndWriteToJson(
            cfcmd, appsUsingBlobstore, encryptionkey)

        uupsViaJson(cfcmd, cfspace)

        restageAppsUsingBlobstore(cfcmd, cfspace, appsUsingBlobstore)

        cflogout(cfcmd)
    }

    void cflogin(String cfcmd, String user, String pwd, String cfspace) {
        def cftarget = 'https://api.devops.geointservices.io'
        def cfskip = '--skip-ssl-validation'
        def cforg = 'piazza'

        def s = "$cfcmd login -a $cftarget "
        s += "-u $user -p $pwd -o $cforg -s $cfspace $cfskip"

        def sx = s.execute()
	println sx.err.text
	println sx.text
    }

	ArrayList getAppsUsingBlobstore(String cfcmd) {
	    def s1 = "$cfcmd services".execute() | "grep pz-blobstore".execute()
	    def t1 = s1.text.split()
	
	    def a = []
	    for (int i=2; i<t1.size(); i++) {
	        //sometimes the split() returns an empty string
	        if (t1[i] != '') {
		    a.add(t1[i].replaceAll(',', ''))
	        }
	    }
            a
	}

        HashMap cfenv(String cfcmd, ArrayList appsUsingBlobstore) {
	    String arbitraryApp = appsUsingBlobstore[0]

            def cmd3 = "$cfcmd env $arbitraryApp"
            def cmdtext3 = cmd3.execute()
	    println cmdtext3.err.text

	    def lines3 = cmdtext3.text.split('\n')
	    int lineContainingAppblobstore
	    for (int i=0; i<lines3.size(); i++) {
	        if (lines3[i] =~ /app-blobstore/) {
		    lineContainingAppblobstore = i
	        }
	    }

	    def credentials = [:]
	    if ((lineContainingAppblobstore < 5) ||
 	        (lineContainingAppblobstore > lines3.size() - 5)) {
	        println "Error: Unexpected 'cf env' output format."
	        return
	    }
	    for (int i=lineContainingAppblobstore-5; 
		 i<=lineContainingAppblobstore+5; i++) {
	        String withoutComma = lines3[i].replaceAll(',', '')
	        [
	         //These are the expected four credential variables
	         // for pz-blobstore. If new ones are introduced
	         // they must be added to this list, because
	         // this code does not interpret the 'cf env' json.
	         'access_key_id', 
	         'bucket', 
	         'path',
	         'secret_access_key'
	        ].each {
	            if (lines3[i] =~ it) credentials[it] = withoutComma
	        }
	    }
            credentials
        }

        void writeToUupsJson(HashMap credentials) {
	    println "About to write to uups json..."
	    def f = new File('upload-uups.json')
	    f.write "{\n"
	    f << "${credentials['access_key_id']},\n"
	    f << "${credentials['bucket']},\n"
	    f << "${credentials['encryption_key']},\n"
	    f << "${credentials['path']},\n"
	    f << "${credentials['secret_access_key']}\n"
	    f << "}\n"
        }

        void getUpsCredentialsAndWriteToJson(
            String cfcmd, ArrayList appsUsingBlobstore,
            String encryptionkey) {

            HashMap upsCredentials = cfenv(cfcmd, appsUsingBlobstore)
	    upsCredentials['encryption_key'] = 
                '     "encryption_key": "' + encryptionkey + '"'

            writeToUupsJson(upsCredentials) 
        }

    void uupsViaJson(String cfcmd, String cfspace) {
        println "About to uups pz-blobstore in $cfspace..."
        def cmd4 = "$cfcmd uups pz-blobstore -p upload-uups.json"
        def cmdtext4 = cmd4.execute()
	println cmdtext4.err.text
	println cmdtext4.text
    }

    void restageAppsUsingBlobstore(
        String cfcmd, String cfspace, 
        ArrayList appsUsingBlobstore) {

	appsUsingBlobstore.each {
	    println "About to restage $cfspace $it"
            def cmd5 = "$cfcmd restage $it"
/* turn off while refactoring
            def cmdtext5 = cmd5.execute()
	    println cmdtext5.err.text
	    println cmdtext5.text
*/
	}
    }

    void cflogout(String cfcmd) {
        def cmd2 = "$cfcmd logout"
        def cmdtext2 = cmd2.execute()
	println cmdtext2.err.text
	println cmdtext2.text
    }

    void createOneEncryptionKeyPerPCFInstance(args) {
	def (
	  ignore,		//name of method to invoke
	  createKey,		//'createKey' or anything else (e.g. 'false')
	  encryptionKeyId,	//e.g. zzzzzzz9-99z9-9999-9999-99zz999999z9
                                // ignored if createKey is 'createKey'
	  encryptionKeyAlias,   //e.g. projectname-kms-pcfname
	  pcfSpaces,		//must omit spaces, e.g. test,dev,int,stage,prod
	  awsAccount,		//e.g. 999999999999
	  awsAccountPowerUser,	//e.g. deisenhower
          region                //e.g. us-east-1
	) = args

	createKeyAndAlias(
            createKey, encryptionKeyAlias, 
            encryptionKeyId, region)

        buildAndWriteToFilePutKeyPolicy(
            pcfSpaces, awsAccount, awsAccountPowerUser, 
            region, encryptionKeyId)

        executeFromFilePutKeyPolicy()
    }

    void createKeyAndAlias(
        String createKey, String encryptionKeyAlias, 
        String encryptionKeyId, String region) {

 	if (createKey == 'createKey') {
	    encryptionKeyId = createKey(encryptionKeyAlias, region)
	}

        createAlias(encryptionKeyAlias, encryptionKeyId, region)
    }

    void buildAndWriteToFilePutKeyPolicy(
        String pcfSpaces, String awsAccount, String awsAccountPowerUser, 
        String region, String encryptionKeyId) {

	String allPCFBlobstoreUserids = 
	    getAllPCFBlobstoreUserids(pcfSpaces, awsAccount)

	String policy = 
            buildPolicyJSON(
               awsAccountPowerUser, awsAccount, allPCFBlobstoreUserids)

	writeToFilePutKeyPolicy(region, encryptionKeyId, policy)
    }

    void createKey(String encryptionKeyAlias, String region) {
        String s = 'aws kms create-key '
        s += "--description $encryptionKeyAlias "
	s += "--region $region "
	s += '--output text'
	def sx = s.execute()
        sx.text.split()[6] 
    }

    void createAlias(
        String encryptionKeyAlias, String encryptionKeyId, 
        String region) {

        def s = 'aws kms create-alias '
	s += "--alias-name alias/$encryptionKeyAlias "
	s += "--target-key-id $encryptionKeyId "
	s += "--region $region"
        def sx = s.execute()
        println ""
	println s
        println "create-alias err.text: ${sx.err.text}"
        println "create-alias    .text: ${sx.text}"
    }

    String buildPolicyJSON(
        String awsAccountPowerUser, String awsAccount, 
        String allPCFBlobstoreUserids) {

	String policy = new File('upload.json').text
        policy = policy.replaceAll('AWSACCOUNTPOWERUSER', awsAccountPowerUser)
        policy = policy.replaceAll('AWSACCOUNT', awsAccount)
        policy = policy.replaceAll('ALLPCFBLOBSTOREUSERIDS', allPCFBlobstoreUserids)
    }

    String getAllPCFBlobstoreUserids(
        String pcfSpaces, String awsAccount) {

	def spaces = pcfSpaces.split(',')
	def awsAccountuser = "arn:aws:iam::$awsAccount:user"

	def allPCFBlobstoreUserids =  ''
	spaces.each {
	    def s1 = 
                "aws iam list-users --output text".execute() | 
                "grep S3BlobstoreUser".execute() | 
                "grep $it".execute()
	    def t1 = s1.text.split()
	    def t2 = t1[5] //gsn-iam-dev-S3BlobstoreUser-99Z9ZZZZ9ZZZZ
	    allPCFBlobstoreUserids += "\"$awsAccountuser/$t2\","
	}

	def r1 = allPCFBlobstoreUserids[0..-2] //trailing comma isn't valid json
	def r2 = r1[1..-2] //leading and trailing double quote aren't needed
	r2
    }

    void writeToFilePutKeyPolicy(region, encryptionKeyId, policy) {
        String s = "aws kms put-key-policy --key-id $encryptionKeyId "
	s += "--region $region --policy-name default "
	s += "--policy '$policy'"

        //This script isn't able to submit the put-key-policy command.
        // The problem is probably related to Groovy's handling of embedded
        // whitespace in .execute() command strings.
	new File('upload-puts').write "$s\n"
    }

    void executeFromFilePutKeyPolicy() {
        def s = "./upload-puts".execute()
        println ""
        println "upload-puts err.text: ${s.err.text}"
        println "upload-puts    .text: ${s.text}"
    }

    //This code is borrowed from
    //  http://docs.aws.amazon.com/AmazonS3/latest/dev/UploadObjSingleOpJava.html

    //Here is the CLI command that is somewhat equivalent
    // to the following code:
    //  aws s3 cp craigdeleteme s3://s3-encryption-dgr
    //   --region us-east-1 --sse-kms-key-id alias/craigkey --sse aws:kms
    def upload(args) {
	//interpret -D command line parms:
	def awsCreds = 
	  new SystemPropertiesCredentialsProvider().getCredentials() 

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

//The following statement executes the method 
// passed in as the args[0] parameter.
new UploadObjectSingleOperation().{args[0]}(args)
