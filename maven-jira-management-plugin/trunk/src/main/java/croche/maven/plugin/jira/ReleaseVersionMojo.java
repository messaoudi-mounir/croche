/*
 * Copyright © 2012 Conor Roche
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package croche.maven.plugin.jira;

import org.apache.maven.plugin.logging.Log;

import com.atlassian.jira.rpc.soap.client.JiraSoapService;

/**
 * The ReleaseVersionMojo is a mojo that can be ran on deploy and will release
 * the jira version corresponding to the deployed maven project version and then will create the next version
 * and update the fix version of any issues to reference the new version
 * @version $Id$
 * @author conorroche
 * @goal release-jira-version
 * @phase deploy
 */
public class ReleaseVersionMojo extends AbstractJiraMojo {

	/**
	 * JIRA Project Key.
	 * @parameter expression="${jiraProjectKey}"
	 */
	String jiraProjectKey;

	/**
	 * This is the optional version prefix to prefix the jira component version with
	 * @parameter expression="${jiraVersionPrefix}" default-value=""
	 */
	String jiraVersionPrefix;

	/**
	 * This is the version type, supported values are regex or sprint_y3d or 2d or 3d
	 * 1. regex means you specify a regex for determining which number of the existing version to increment
	 * 2. sprint_y3d is where the existing version is expected to be in the form yyyy-QX.Y.Z or yyyy-QX.Y or yyyy-QX.Y.Z-SNAPSHOT or yyyy-QX.Y-SNAPSHOT
	 * 3. 2d is where the existing version string should contain 2 separate numbers, the 2nd one will be incremented
	 * 4. 3d is where the existing version string should contain 3 separate numbers, the 3rd one will be incremented
	 * @parameter versionType="${versionType}"
	 * @required
	 */
	String versionType;

	/**
	 * This is the regex pattern used for the next release e.g. the version number
	 * used after the release has been made, the nextVersionGroup is the number of the group in this
	 * pattern used as the portion that should be incremented/replaced with the nextVersionReplacement.
	 * This is optional and if not present means no next version will be created
	 * @parameter expression="${nextVersionRegex}"
	 */
	String nextVersionRegex;
	/**
	 * This is the number of the group in the nextVersionRegex pattern that is the portion that is incremented/replaced with the nextVersionReplacement
	 * after release for the next development version. If the nextVersionRegex is specified then this will
	 * default to 1 if not specified.
	 * @parameter expression="${nextVersionGroup}" default-value="1"
	 */
	Integer nextVersionGroup = 1;
	/**
	 * This is the replacement string to use for the regex group identified by nextVersionGroup, this is optional
	 * and if NOT specified it defaults to INCREMENT which means it will increment the number identified by the nextVersionGroup.
	 * It may be set to GROUP_TEXT which means it will be set to the current value of the group and hence not replaced.
	 * @parameter expression="${nextVersionReplacement}" default-value="INCREMENT"
	 */
	String nextVersionReplacement = "INCREMENT";

	/**
	 * This is the whether to update the fix version of any issues referencing the release version to the next version, default is true
	 * but only applies when next version is to be created
	 * @parameter expression="${moveIssuesToNextJiraVersion}" default-value="true"
	 */
	boolean moveIssuesToNextJiraVersion = true;

	/**
	 * This is the max number of issues that will be updated
	 * @parameter expression="${maxIssuesToUpdate}" default-value="100"
	 */
	Integer maxIssuesToUpdate = 100;

	/**
	 * {@inheritDoc}
	 * @see croche.maven.plugin.jira.AbstractJiraMojo#doExecute(com.atlassian.jira.rpc.soap.client.JiraSoapService, java.lang.String)
	 */
	@Override
	public void doExecute(JiraSoapService jiraService, String loginToken) throws Exception {
		Log log = getLog();
		log.info("ReleaseVersionMojo current projectVersion: " + this.projectVersion);

		JiraVersionManager versionManager = new JiraVersionManager(jiraService, loginToken, getLog());
		JiraVersionSpec versionSpec = new JiraVersionSpec();
		versionSpec.setExistingVersion(this.projectVersion);
		versionSpec.setJiraProjectKey(this.jiraProjectKey);
		versionSpec.setJiraVersionPrefix(this.jiraVersionPrefix);
		versionSpec.setNextVersionGroup(this.nextVersionGroup);
		versionSpec.setNextVersionRegex(this.nextVersionRegex);
		versionSpec.setNextVersionReplacement(this.nextVersionReplacement);
		versionSpec.setMaxIssuesToUpdate(this.maxIssuesToUpdate);
		versionSpec.setVersionType(this.versionType);

		if (this.projectVersion.contains("SNAPSHOT")) {
			log.info("ReleaseVersionMojo not releasing jira version as projectVersion: " + this.projectVersion
					+ " is a snapshot, however matching jira version will be created if necessary");
			versionManager.optionallyCreateVersion(versionSpec);
		} else {

			versionManager.releaseVersion(versionSpec);
		}

	}

}
