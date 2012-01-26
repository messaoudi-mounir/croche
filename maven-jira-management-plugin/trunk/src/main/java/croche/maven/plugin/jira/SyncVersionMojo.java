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

import com.atlassian.jira.rpc.soap.client.RemoteVersion;
import com.atlassian.jira.rpc.soap.client.JiraSoapService;

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
	 * This is the optional version prefix to prefix the maven component version with
	 * @parameter expression="${versionPrefix}" default-value=""
	 */
	String versionPrefix;

	/**
	 * @parameter expression="${projectVersion}" default-value="${project.version}"
	 * @readonly
	 * @required
	 */
	String projectVersion;
	
	/** 
	 * {@inheritDoc}
	 * @see croche.maven.plugin.jira.AbstractJiraMojo#doExecute(com.atlassian.jira.rpc.soap.client.JiraSoapService, java.lang.String)
	 */
	@Override
	public void doExecute(JiraSoapService jiraService, String loginToken) throws Exception {

		// get the name of the version to use for this project
		String projectVersion = getProjectVersion().replace("-SNAPSHOT", "");
		
		String jiraVersion = getVersionPrefix() + projectVersion;
		
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
	 * This gets the versionPrefix
	 * @return the versionPrefix
	 */
	public String getVersionPrefix() {
		return this.versionPrefix.replace("#space", " ");
	}

	/**
	 * This sets the versionPrefix
	 * @param versionPrefix the versionPrefix to set
	 */
	public void setVersionPrefix(String versionPrefix) {
		this.versionPrefix = versionPrefix;
	}

	/**
	 * This gets the projectVersion
	 * @return the projectVersion
	 */
	public String getProjectVersion() {
		return this.projectVersion;
	}

	/**
	 * This sets the projectVersion
	 * @param projectVersion the projectVersion to set
	 */
	public void setProjectVersion(String projectVersion) {
		this.projectVersion = projectVersion;
	}
	
	/**
	 * Check if version is already present
	 * @param versions The versions to check against
	 * @param version The version to check against
	 * @return True if it exists
	 */
	boolean isVersionAlreadyPresent(RemoteVersion[] versions, String version) {
		boolean versionExists = false;
		if (versions != null) {
			// Creating new Version (if not already created)
			for (RemoteVersion remoteVersion : versions) {
				if (remoteVersion.getName().equalsIgnoreCase(version)) {
					versionExists = true;
					break;
				}
			}
		}
		return versionExists;
	}

}
