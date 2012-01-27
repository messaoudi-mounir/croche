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
 * The ReleaseMultipleJiraVersionsMojo represents a mojo that supports releasing several configured jira versions,
 * this can be used as part of an overall platform release project and allows jira versions to be updated for
 * serveral projects/components in one go.
 * @version $Id$
 * @author conorroche
 * @goal release-multiple-jira-versions
 * @phase deploy
 */
public class ReleaseMultipleJiraVersionsMojo extends AbstractJiraMojo {

	/**
	 * This defines the verions that should be released
	 * @parameter expression="${jiraVersionSpecs}"
	 * @required
	 */
	JiraVersionSpec[] jiraVersionSpecs;

	/**
	 * {@inheritDoc}
	 * @see croche.maven.plugin.jira.AbstractJiraMojo#doExecute(com.atlassian.jira.rpc.soap.client.JiraSoapService, java.lang.String)
	 */
	@Override
	public void doExecute(JiraSoapService jiraService, String loginToken) throws Exception {

		boolean isBranch = this.scmConnection != null && this.scmConnection.contains("branches");

		Log log = getLog();
		log.info("ReleaseMultipleJiraVersionsMojo current projectVersion: " + this.projectVersion + ", scmConnection: " + this.scmConnection);
		JiraVersionManager versionManager = new JiraVersionManager(jiraService, loginToken, getLog());

		if (this.jiraVersionSpecs == null || this.jiraVersionSpecs.length == 0) {
			log.warn("ReleaseMultipleJiraVersionsMojo not releasing jira versions as no versions were configured in the configuration.");
		} else if (this.projectVersion.contains("SNAPSHOT")) {
			log.info("ReleaseMultipleJiraVersionsMojo not releasing jira versions as projectVersion: " + this.projectVersion
					+ " is a snapshot but will create any existing versions if necessary");
			for (JiraVersionSpec versionSpec : this.jiraVersionSpecs) {
				versionManager.optionallyCreateVersion(versionSpec);
			}
		} else {

			for (JiraVersionSpec versionSpec : this.jiraVersionSpecs) {
				versionManager.releaseVersion(versionSpec, isBranch);
			}

		}

	}

}
