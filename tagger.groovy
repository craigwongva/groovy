package tag

import groovy.json.*

class Tagger {

    void servers() {

	def runtypes = ['instances', 'images']
	//def runtype = 'images'
	def runtype = 'instances'

	def untaggedInstances = [:]



	//def regions = ['us-west-2'] 
	def regions = ['us-east-1']
	//def regions = ['us-west-1']
	//def regions = ['us-west-2','us-east-1','us-west-1']
	regions.each { region ->

	    def allProjectValuesAsNameCRLFValue = []
	    //The following returns a literal 'Project' and then a value like 'beachfront-int' on the same line.

	    def getAllProjectValuesAsNameCRLFValue
	    if (runtype == 'instances') {
                getAllProjectValuesAsNameCRLFValue = "aws ec2 describe-instances --filter Name=tag-key,Values=Project --query Reservations[].Instances[].[Tags] --region $region --output text --max-items 20000 ".execute()
		//--instance-ids i-0ac00b1664b15426a i-c35133f2
	    }
	    if (runtype == 'images') {
                getAllProjectValuesAsNameCRLFValue = "aws ec2 describe-images --filter Name=tag-key,Values=Project --query Images[].[Tags] --region $region --output text".execute()
	    }
    	    allProjectValuesAsNameCRLFValue = getAllProjectValuesAsNameCRLFValue.text.split()
	    println "69: $allProjectValuesAsNameCRLFValue"
/*
69: [aws:cloudformation:stack-id, arn:aws:cloudformation:us-east-1:539674021708:stack/celery2/dcadd920-79d3-11e6-ab8d-500c5240582a, aws:cloudformation:stack-name, celery2, Project, eventkit-dev, aws:cloudformation:logical-id, Server, Project, geowave-dev]
*/

    	    int allProjectValuesAsNameCRLFValueSize = allProjectValuesAsNameCRLFValue.size()
	    println "77: $allProjectValuesAsNameCRLFValueSize"
	    def myfile = new File('deleteme333')
    
    	    def allProjectValues = []
    	    for (int i=0; i<allProjectValuesAsNameCRLFValueSize; i += 2) {
		myfile << "$i,${allProjectValuesAsNameCRLFValue[i]}\n"
		myfile << "${i+1},${allProjectValuesAsNameCRLFValue[i+1]}\n"
    	        if (allProjectValuesAsNameCRLFValue[i] == 'Project') {
    		    allProjectValues.add(allProjectValuesAsNameCRLFValue[i+1])
    	        }
    	    }
    
	    println "83: $allProjectValues"
/*
83: [eventkit-dev, geowave-dev]
*/
    	    //Use a list of allInstances to determine which instancs are untagged,
    	    // because the CLI doesn't seem to allow the same wildcards that the EC2 Dashboard GUI filter window allows
    	    // e.g. "Not tagged" in "tag:Project : Not tagged"
    	    def allInstances = []
    	    def allTagged = [:]
    	    //The following returns one InstanceId value per line.

	    //AMIs: aws ec2 describe-images --filter Name=owner-id,Values=539674021708 --query Images[].[ImageId] --region us-west-2 --output text

	    def all
	    if (runtype == 'instances') {
    	        all = "aws ec2 describe-instances --query Reservations[].Instances[].[InstanceId] --region $region --output text".execute()
	    }
	    if (runtype == 'images') {
    	        all = "aws ec2 describe-images --filter Name=owner-id,Values=539674021708 --query Images[].[ImageId] --region $region --output text".execute()
	    }
    	    allInstances = all.text.split()

	    println "104 ${allInstances.size()}"
/*
104 [i-7b2fa3e2, i-0d9211114c0ad285f, i-09751d38, i-0edfe0a195902c776, i-7309f3e2, i-05c53b51862377501, i-00ae6a91, i-cdb2a753,
*/    
    	    //Sort the Project values (e.g. beachfront-dev, piazza-int) just for println readability.
    	    def projects = allProjectValues.unique().sort()
    	    projects.each { p ->
    	        //The following returns one InstanceID value per line.

		//AMIs: aws ec2 describe-images --filter Name=owner-id,Values=539674021708 Name=tag:Project,Values=piazza-dev --query Images[].[ImageId] --region us-east-1 --output text

		def tagged
	        if (runtype == 'instances') {
    	            tagged = "aws ec2 describe-instances --filter Name=tag:Project,Values=$p --query Reservations[].Instances[].[InstanceId] --region $region --output text".execute()
		}
	        if (runtype == 'images') {
    	            tagged = "aws ec2 describe-images --filter Name=owner-id,Values=539674021708 Name=tag:Project,Values=$p --query Images[].[ImageId] --region $region --output text".execute()
		}
    	        allTagged[p] = tagged.text.split()
		println "124: allTagged[$p] is ${allTagged[p]}"
    	    }
    
    	    untaggedInstances[region] = allInstances
    	    projects.each { p ->
    	        allTagged[p].each { i ->
		    def ilong = (i.size() == 10)? i+'         ' : i
    		    println "$region|$ilong|$p"
    	        }
    	        untaggedInstances[region] -= allTagged[p]
    	    }
    	    untaggedInstances[region].each { i ->
		def ilong = (i.size() == 10)? i+'         ' : i
    	        println "$region|$ilong|untagged"
    	    }
	}

	if (runtype == 'instances') {
	    println "untaggedInstances:\n$untaggedInstances"
	}
	if (runtype == 'images') {
	    println "untaggedImages:\n$untaggedInstances"
	}
/*
	//us-east-1 can take about 60 seconds to generate the untaggedInstances,
	// so sometimes I generate the list once and then comment out that code
	// and then insert a hardcoded ArrayList of untagged instances below.
	//Two small example lists are shown here.
	untaggedInstances['us-west-2'] = ['i-0f59fea1', 'i-507ba50f']
	untaggedInstances['us-east-1'] = ['i-030eb255a5506fdaf', 'i-0a4bcba9650f701dc']
*/


        def tagme = [:]
	tagme['us-west-1'] = [
	    //RULES FOR EC2 INSTANCES
	      'Name=instance.group-name,Values=legion-web' : 'xterrain-dev',
               //Name=instance.group-name,Values=legion-web
	    //RULES FOR AMI IMAGES
              'Name=name,Values=dual-geoserver'    :'geoserver-dev',
              'Name=name,Values=geoserver*'        :'geoserver-dev',
              'Name=name,Values=gs*'               :'xterrain-dev',
              'Name=name,Values=legion*'           :'legion-dev',
              'Name=name,Values=xterrain*'         :'xterrain-dev',
	]
	tagme['us-west-2'] = [
	    //RULES FOR EC2 INSTANCES
	      //Use the key-pair to infer the project:
	      'Name=key-name,Values=adam-chou-key' :'piazza-dev',
	      'Name=key-name,Values=afroje-initial':'piazza-dev',
	      'Name=key-name,Values=akey-initial'  :'piazza-dev',
	      'Name=key-name,Values=ccri-gs'       :'xterrain-dev',
	      'Name=key-name,Values=craigradiantblueoregon':'piazza-dev',
	      'Name=key-name,Values=crunchy-admin' :'crunchy-dev',
	      'Name=key-name,Values=gs-salt*'      :'unknown-dev',
	      'Name=key-name,Values=peizer'        :'beachfront-dev',
	      'Name=key-name,Values=smithwinserve' :'piazza-dev',
	      'Name=key-name,Values=stresstest'    :'unknown-dev',
	    //RULES FOR AMI IMAGES
              'Name=name,Values=adam-do-not-use'   :'piazza-dev',
              'Name=name,Values=craig*'            :'piazza-dev',
              'Name=name,Values=pz-prime**'        :'piazza-dev',
              'Name=name,Values=shb-forensics'     :'piazza-dev',
              'Name=name,Values=venice-bastion*'   :'piazza-dev',
              'Name=name,Values=venice-bosh*'      :'piazza-dev',
              'Name=name,Values=venice-opensextant*' :'piazza-dev',
              'Name=name,Values=venice-solr*'      :'piazza-dev',
	]
	tagme['us-east-1'] = [
	    //RULES FOR EC2 INSTANCES
	      //Use the key-pair to infer the project:
	      'Name=key-name,Values=celery*'       :'eventkit-dev',
	      'Name=key-name,Values=geowave*'      :'geowave-dev',
	      'Name=key-name,Values=gsp-vpc'       :'piazza-dev',
	      'Name=key-name,Values=legion*'       :'legion-dev',
	      'Name=key-name,Values=mrgeo'         :'mrgeo-dev',
	      'Name=key-name,Values=packer*'       :'piazza-dev',
	      //Use the tag Name to infer the project:
	      'Name=tag:Name,Values=gsp-bastion'   :'piazza-dev',
	      'Name=tag:Name,Values=idaho*'        :'gbdx-dev',
	    //RULES FOR AMI IMAGES
              'Name=name,Values=bastion*'          :'piazza-dev',
              'Name=name,Values=craigsami*'        :'piazza-dev',
              'Name=name,Values=crunchy*'          :'piazza-dev',
              'Name=name,Values=gbdx*'             :'gbdx-dev',
              'Name=name,Values=geoserver*'        :'geoserver-dev',
              'Name=name,Values=geoshape*'         :'geoshape-dev',
              'Name=name,Values=gs*'               :'xterrain-dev',
              'Name=name,Values=jenkins*'          :'piazza-dev',
              'Name=name,Values=kafka*'            :'piazza-dev',
              'Name=name,Values=ldap*'             :'piazza-dev',
              'Name=name,Values=legion*'           :'legion-dev',
              'Name=name,Values=mrgeo*'            :'mrgeo-dev',
              'Name=name,Values=packer*'           :'packer-dev',
              'Name=name,Values=pz-base*'          :'piazza-dev',
              'Name=name,Values=pz-bastion*'       :'piazza-dev',
	]

	untaggedInstances.each { uk, uv ->
	    def untaggedInstancesAsArrayList = uv.collect{it}

	    tagme[uk].each { rk, rv ->
	        //The following returns a string of whitespace-separated InstanceIds.

	        def peizerWhitespaceSeparatedInstanceIds
	        if (runtype == 'instances') {
	            peizerWhitespaceSeparatedInstanceIds = "aws ec2 describe-instances --filter $rk --query Reservations[].Instances[].InstanceId --region $uk --output text".execute()
		}
	        if (runtype == 'images') {
	            peizerWhitespaceSeparatedInstanceIds = "aws ec2 describe-images --filter $rk --query Images[].ImageId --region $uk --output text".execute()
		}

	        def peizerInstanceIds = peizerWhitespaceSeparatedInstanceIds.text.split()
	        def peizerInstanceIdsAsArrayList = peizerInstanceIds.collect{it}
	        def intersekt = peizerInstanceIdsAsArrayList.intersect(untaggedInstancesAsArrayList)
		if (intersekt.size() > 0) {
	            def mycmd = "aws ec2 create-tags --dry-run --resources ${intersekt.join(' ')} --tags Key=Project,Value=$rv --region $uk " 
	            println mycmd
		}
	    }
	}
    }
}

def h = new Tagger()
h.servers()
