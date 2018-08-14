### Purpose

Stand up a Groovy for any lab usage.

### Usage
```
# From your c9 instance:
cd ~/environment
git clone https://github.com/cwva/groovy
cd groovy
./launch groovy1
```

### Test
* Look at the last /tmp/hello-xxxx file. It will contain a stack
trace. This is due to the code looking for a particular Cloud
Watch item, which no longer exists.
* But the fact that the Groovy executes proves that Groovy is
available.
