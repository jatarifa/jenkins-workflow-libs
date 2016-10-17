/**
 * VDI rest client
 */
class VDI implements Serializable {

	private def steps
	private def jobName
	private def env

	/**
	*	Constructor that loads also VDI env variables
	*	@param theJobName Name of this VDI instances group
	*	@param theStep step variable from Jenkins
	*	@param theEnv env variable form Jenkins
	*/
	def VDI(theJobName, theStep, theEnv) {
		steps = theStep
		jobName = theJobName
		env = theEnv
		new VDIEnvironment(theEnv)
	}

	/**
	*	Create a new VDI instance
	*	@param descInstance Name of this particular VDI instance
	*	@param instanceType AWS instance type.  Defaults : t2.micro
	*	@param imageId AMi to use.  Defaults : ami-b76b96da
	*	@return InstanceId of the VDI instance
	*/
    def createInstance(descInstance = '', instanceType = 't2.micro', imageId = 'ami-b76b96da') {
		def product=env.VDI_INSTANCE_PRODUCT
		def envi=env.VDI_INSTANCE_ENV
		def serviceLine=env.VDI_INSTANCE_SERVICE_LINE
		def securityGroups=env.VDI_INSTANCE_SEC_GROUPS
		def relativeExpiry=env.VDI_INSTANCE_EXPIRY
		def availabilityZone=env.VDI_INSTANCE_AVAILABILITY_ZONE
		def keyName=env.VDI_INSTANCE_KEYNAME
		def subnetId=env.VDI_INSTANCE_SUBNET_ID  

		def name="Jenkins2 - $jobName - $descInstance"
		def desc="Jenkins2 - $jobName - $descInstance"    

    	def response = steps.sh returnStdout: true,
    			 				script: "curl -s -X POST -d \"{ \\\"name\\\":\\\"$name\\\", \\\"desc\\\":\\\"$desc\\\", \\\"imageId\\\":\\\"$imageId\\\", \\\"dailyOff\\\": \\\"never\\\", \\\"idleOff\\\": false, \\\"instanceType\\\":\\\"$instanceType\\\", \\\"product\\\":\\\"$product\\\", \\\"env\\\":\\\"$envi\\\", \\\"serviceLine\\\":\\\"$serviceLine\\\", \\\"securityGroups\\\":[$securityGroups], \\\"relativeExpiry\\\":$relativeExpiry, \\\"availabilityZone\\\":\\\"$availabilityZone\\\", \\\"keyName\\\":\\\"$keyName\\\", \\\"subnetId\\\":\\\"$subnetId\\\" }\" -H \"Content-Type:application/json\" -u \"${env.CREDENTIALS}\" ${env.CREATE_INSTANCE}"
		def object = parseText(response)

	    return object.instanceIds[0]
    }

	/**
	*	Stops VDI instances
	*	@param instanceId IntanceIds to stop
	*	@return Json response
	*/
    def stopInstance(VDINode ... instanceIds) {
    	def instanceId = "\\\"${instanceIds[0].instanceId}\\\""
    	for(int i = 1; i < instanceIds.length; i++)
	    	instanceId += ",\\\"${instanceIds[i].instanceId}\\\""

    	def response = steps.sh returnStdout: true,
    			 				script: "curl -s -X POST -d \"{\\\"instanceIds\\\": [${instanceId}]}\" -H \"Content-Type:application/json\" -u \"${env.CREDENTIALS}\" ${env.STOP_INSTANCE}"
		def object = parseText(response)

	    return object
    }

	/**
	*	Terminates VDI instances
	*	@param instanceId IntanceIds to terminate
	*	@return Json response
	*/
    def terminateInstance(VDINode ... instanceIds) {
    	def instanceId = "\\\"${instanceIds[0].instanceId}\\\""
    	for(int i = 1; i < instanceIds.length; i++)
	    	instanceId += ",\\\"${instanceIds[i].instanceId}\\\""

    	def response = steps.sh returnStdout: true,
    			 				script: "curl -s -X POST -d \"{\\\"instanceIds\\\": [${instanceId}]}\" -H \"Content-Type:application/json\" -u \"${env.CREDENTIALS}\" ${env.TERMINATE_INSTANCE}"
		def object = parseText(response)

	    return object
    }

	/**
	*	Describe a VDI instance
	*	@param instanceId IntanceId to describe
	*	@return Json response
	*/
    def describeInstance(instanceId) {
    	def object
    	def url = sprintf(env.DESCRIBE_INSTANCE, instanceId)

    	steps.retry(3)
    	{
    		steps.sh "sleep 10"
	    	def response = steps.sh returnStdout: true,
	    			 				script: "curl -s -u \"${env.CREDENTIALS}\" $url"
			object = parseText(response)
		}
	    return object
    }

	/**
	*	Wait to have all instancesId up and accesible via SSH
	*	@param secons_timeout Timeout to wait
	*	@param instances Instances to wait for
	*/
    def waitForInstancesRunning(seconds_timeout = 430, String ... instances) {
     	steps.sh "sleep 90"
   		def iters = seconds_timeout - 90

   		instances.each {
			def status = 1
			while(status != 0 && --iters > 0) {
			    status = steps.sh returnStatus: true, 
			                      script: "sleep 1; timeout 1 bash -c \'cat < /dev/null > /dev/tcp/$it/22\'"
			}

			if(status != 0)
			    steps.error "Instance $it running status timeout"
    	}
    }

	/**
	*	Updates hostname and host file from a VDI ip
	*	@param ip IP of the VDI
	*	@param host Hostname to set
	*	@param host_hosrt Short hostname to set
	*/
	def updateHost(ip, host, host_short) {
		ssh(ip, "hostname $host")
		ssh(ip, "echo $ip $host $host_short >> /etc/hosts")
	}

	@NonCPS
	private def parseText(txt) {
	    return new groovy.json.JsonSlurperClassic().parseText(txt)
	}

	private def ssh(ip, command) {
		steps.sh "ssh -o StrictHostKeyChecking=no root@$ip \"$command\""
	}
}