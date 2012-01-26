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
import com.atlassian.jira.rpc.soap.client.RemoteVersion;

/**
 * The SyncVersionMojo represents a maven mojo that syncs the version of the project being released
 * with the versions in jira.
 * It will create a jira version if one does not already exist.
 * @version $Id$
 * @author conorroche
 * @goal sync-jira-version
 * @phase deploy
 */
public class SyncVersionMojo extends AbstractJiraMojo {

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
	 * {@inheritDoc}
	 * @see croche.maven.plugin.jira.AbstractJiraMojo#doExecute(com.atlassian.jira.rpc.soap.client.JiraSoapService, java.lang.String)
	 */
	@Override
	public void doExecute(JiraSoapService jiraService, String loginToken) throws Exception {

		// get the name of the version to use for this project
		String projectVersion = getProjectVersion().replace("-SNAPSHOT", "");

		String jiraVersion = getJiraVersionPrefix() + projectVersion;

		Log log = getLog();
		RemoteVersion[] versions = jiraService.getVersions(loginToken, this.jiraProjectKey);

		// check if this version exists in jira, if not create it
		if (!isVersionAlreadyPresent(versions, jiraVersion)) {
			RemoteVersion newVersion = new RemoteVersion();
			log.debug("New Version in JIRA is: " + jiraVersion);
			newVersion.setName(jiraVersion);
			jiraService.addVersion(loginToken, this.jiraProjectKey, newVersion);
			log.info("Version created in JIRA for project key " + this.jiraProjectKey + " : " + jiraVersion);
		} else {
			log.info(String.format("Version %s is already created in JIRA. Nothing to do.", jiraVersion));
		}
	}

	/**
	 * This gets the jiraVersionPrefix
	 * @return the jiraVersionPrefix
	 */
	public String getJiraVersionPrefix() {
		return this.jiraVersionPrefix.replace("#space", " ");
	}

	/**
	 * This sets the jiraVersionPrefix
	 * @param jiraVersionPrefix the jiraVersionPrefix to set
	 */
	public void setJiraVersionPrefix(String jiraVersionPrefix) {
		this.jiraVersionPrefix = jiraVersionPrefix;
	}

}
