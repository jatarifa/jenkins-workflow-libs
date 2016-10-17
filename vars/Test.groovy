/**
 * Litmus Test Wrapper
 */
class Test implements Serializable {

	private def steps
	private def env

	private def packageDir
	private def ambariHost
	private def edwHosts = []
	private def ldapHost
	private def collectorHost
	private def oaeHost
	private def gangliaHost
	private def nagiosHost

	private def headNode = ''
	private def inPlaceUpgrade = ''
	private def noAnalyticsUpgrade = ''
	private def extend = ''
	private def upgrade = ''
	private def config = ''
	private def defaultPrefix = ''
	private def noOae = ''
	private def edwOnly = ''
	private def nonDefaults = ''
	private def dataMirroring = ''
	private def multiInstance = ''
	private def debug = ''
	private def from

	/**
	*	Constructor
	*	@param theStep step variable from Jenkins
	*	@param theEnv env variable form Jenkins
	*/
	def Test(theStep, theEnv) {
		steps = theStep
		env = theEnv
	}

	/**
	*	DSL method setting build-repo package dir
	*	@param dir Dir
	*	@return This instance
	*/
	def withPackageDir(dir) {
		packageDir = dir
		return this
	}

	/**
	*	DSL method setting ambari host
	*	@param ambariHost Ambari host
	*	@return This instance
	*/
	def withAmbariHost(ambariHost) {
		this.ambariHost = ambariHost
		return this
	}

	/**
	*	DSL method adding new EDW host (multiple calls accepted)
	*	@param edwHost Host to add as EDW
	*	@return This instance
	*/
	def withAddEDWHost(edwHost) {
		this.edwHosts.add(edwHost)
		return this
	}

	/**
	*	DSL method setting ldap host
	*	@param ldapHost Ldap host
	*	@return This instance
	*/
	def withLdapHost(ldapHost) {
		this.ldapHost = ldapHost
		return this
	}

	/**
	*	DSL method setting collector host
	*	@param collectorHost Collector host
	*	@return This instance
	*/
	def withCollectorHost(collectorHost) {
		this.collectorHost = collectorHost
		return this
	}

	/**
	*	DSL method setting oae host
	*	@param oaeHost Oae host
	*	@return This instance
	*/
	def withOaeHost(oaeHost) {
		this.oaeHost = oaeHost
		return this
	}

	/**
	*	DSL method setting ganglia host
	*	@param gangliaHost Ganglia host
	*	@return This instance
	*/
	def withGangliaHost(gangliaHost) {
		this.gangliaHost = gangliaHost
		return this
	}

	/**
	*	DSL method setting nagios host
	*	@param nagiosHost Nagios host
	*	@return This instance
	*/
	def withNagiosHost(nagiosHost) {
		this.nagiosHost = nagiosHost
		return this
	}

	/**
	*	DSL method setting EDW Only parameter
	*	@param edwOnly EDW only parameter
	*	@return This instance
	*/
	def withEdwOnly(edwOnly) {
		this.edwOnly = "--sls-only $edwOnly"
		return this
	}

	/**
	*	DSL method setting non defauts
	*	@return This instance
	*/
	def withNonDefaults() {
		this.nonDefaults = '--nondefaults'
		return this
	}

	/**
	*	DSL method setting no data mirroring
	*	@return This instance
	*/
	def withNoDataMirroring() {
		this.dataMirroring = '--no-data-mirroring'
		return this
	}

	/**
	*	DSL method setting no data mirroring
	*	@return This instance
	*/
	def withNoAnalyticsUpgrade() {
		this.noAnalyticsUpgrade = '--no-analytics-upgrade'
		return this
	}

	/**
	*	DSL method setting multi-instance
	*	@return This instance
	*/
	def withMultiInstance() {
		this.multiInstance = '--multi-instance'
		return this
	}

	/**
	*	DSL method setting debug
	*	@return This instance
	*/
	def withDebug() {
		this.debug = '--debug'
		return this
	}

	/**
	*	DSL method setting default prefix
	*	@return This instance
	*/
	def withDefaultPrefix() {
		this.defaultPrefix = '--default-prefix'
		return this
	}

	/**
	*	DSL method setting no OAE
	*	@return This instance
	*/
	def withNoOAE() {
		this.noOae = '--no-oae'
		return this
	}

	/**
	*	DSL method setting config
	*	@return This instance
	*/
	def withConfig(config) {
		this.config = '--config ' + config
		return this
	}

	/**
	*	DSL method setting an upgrade
	*	@return This instance
	*/
	def withUpgrade(version) {
		this.upgrade = '--upgrade ' + version
		return this
	}

	/**
	*	DSL method setting an extend
	*	@return This instance
	*/
	def withExtend(hosts) {
		this.extend = '--extend ' + hosts
		return this
	}

	/**
	*	DSL method setting head node
	*	@return This instance
	*/
	def withHeadNode(node) {
		this.headNode = '--head-node ' + node
		return this
	}

	/**
	*	DSL method setting inplace upgrade
	*	@return This instance
	*/
	def withInPlaceUpgrade() {
		this.inPlaceUpgrade = '--inplace-upgrade'
		return this
	}

	/**
	*	DSL method setting from where to start install
	*	@param from IP from where to start to install
	*	@return This instance
	*/
	def withInstallFrom(from) {
		this.from = from
		return this
	}

	/**
	*	DSL method to launch a test
	*	@param test Test to launch
	*/
	def test(test) {
		ssh(from, '''bash -c \'cat << EOF > /etc/yum.repos.d/rhel-local.repo
[local_rhel]
name=local rhel
baseurl=http://fileserver.sensage.com/kickstart/rhel6.7
gpgcheck=0
EOF
\'''')

		ssh(from, '''bash -c \'cat << EOF > /etc/yum.repos.d/file.repo
[file_rhel]
name=file rhel
baseurl=http://build-repo.sensage.com/hawkeye-ap/el5/optimized_unstripped/automation/develop/
gpgcheck=0
EOF
\'''')

		ssh(from, "mkdir ./automation")
		steps.sh "scp -r automation root@$from:./"
		ssh(from, "wget -q http://build-repo.sensage.com/hawkeye-ap/el6/optimized_unstripped/tars/${packageDir}/latest_el6.tgz")
		ssh(from, "cp -f ./automation/litmus/litmusFramework/scripts/new_litmus_run_master_jenkins.sh ./new_litmus_run_master.sh")
		ssh(from, "cp -f ./automation/tools/qa_install_product_jenkins.sh ./qa_install_product.sh")
		ssh(from, "chmod +x *.sh")

		def edwList = ''
		for(host in edwHosts)
			edwList += host + " "

		ssh(from, "./new_litmus_run_master.sh --build latest_el6.tgz --ambariHost ${ambariHost} --edwHosts ${edwList} --ldapHost ${ldapHost} --oaeHost ${oaeHost} --collectorHost ${collectorHost} --gangliaHost ${gangliaHost} --nagiosHost ${nagiosHost} ${inPlaceUpgrade} ${headNode} ${noAnalyticsUpgrade} ${extend} ${upgrade} ${config} ${noOae} ${defaultPrefix} ${edwOnly} ${nonDefaults} ${dataMirroring} ${multiInstance} ${debug} --test-list $test")
    }

    private def ssh(ip, command) {
		steps.sh "ssh -o StrictHostKeyChecking=no root@$ip \"$command\""
	}
}