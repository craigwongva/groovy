| 1 | 2 | 3 | 4 |
|--- | --- | --- | --- |
| CloudFormation
 | calls S3 redf4rth-groovy cf-groovy.json | | 
S3 redf4rth-groovy | | | 
 | cf-groovy.json | | 
 | | is same as github groovy cf-groovy.json | 
 | dotsdkman.zip | | 
 | | is called by userdata-groovy | 
github groovy | | | 
 | groovy | | 
 | | cf-groovy.json | 
 | | | uses security group
 | | | calls userdata-groovy
 | | hello.groovy | 
 | | | is called by userdata-groovy
 | | userdata-groovy | 
 | | | is called by cf-groovy.json
 | | | calls hello.groovy
