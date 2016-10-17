/**
 * VDI default parameters
 */
class VDIEnvironment implements Serializable {

	/**
	*	Set as environment variables, the VDI default parameters
	*/
	def VDIEnvironment(theEnv)
	{
		def env = theEnv
		env.VDI_AWS_ACCOUNT_ID=1
		env.VDI2_ACCOUNT_LOGIN=""
		env.VDI2_ACCOUNT_PASSWORD=""
		env.VDI_INSTANCE_PRODUCT="Sensage"
		env.VDI_INSTANCE_ENV="QA"
		env.VDI_INSTANCE_SERVICE_LINE="Other"
		env.VDI_INSTANCE_SEC_GROUPS="\\\"sg-e21de799\\\",\\\"sg-851ce6fe\\\",\\\"sg-3712e84c\\\""
		env.VDI_INSTANCE_EXPIRY=21600000
		env.VDI_INSTANCE_AVAILABILITY_ZONE="us-east-1a"
		env.VDI_INSTANCE_KEYNAME="sensage-dev-admin"
		env.VDI_INSTANCE_SUBNET_ID="subnet-c5452d9d"

		env.VDI_REST_URI="http://vdi2.devfactory.com/rest/${env.VDI_AWS_ACCOUNT_ID}"
		env.CREDENTIALS="${env.VDI2_ACCOUNT_LOGIN}:${env.VDI2_ACCOUNT_PASSWORD}"
		env.CREATE_INSTANCE="${env.VDI_REST_URI}/instance/create"
		env.DESCRIBE_INSTANCE="${env.VDI_REST_URI}/instance/%s/describe"
		env.STOP_INSTANCE="${env.VDI_REST_URI}/instance/stop"
		env.TERMINATE_INSTANCE="${env.VDI_REST_URI}/instance/terminate"
	}
}