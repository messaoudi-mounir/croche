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

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.plugin.logging.Log;

import com.atlassian.jira.rpc.soap.client.JiraSoapService;
import com.atlassian.jira.rpc.soap.client.RemoteException;
import com.atlassian.jira.rpc.soap.client.RemoteFieldValue;
import com.atlassian.jira.rpc.soap.client.RemoteIssue;
import com.atlassian.jira.rpc.soap.client.RemoteVersion;

/**
 * The JiraVersionManager represents a jira version manager
 * @version $Id$
 * @author conorroche
 */
public class JiraVersionManager {

	private static final int ISSUES_PER_SEARCH = 30;

	private final JiraSoapService jiraService;
	private final String loginToken;
	private final Log log;

	/**
	 * This creates a JiraVersionManager
	 * @param jiraService
	 * @param loginToken
	 * @param log
	 */
	public JiraVersionManager(JiraSoapService jiraService, String loginToken, Log log) {
		super();
		this.jiraService = jiraService;
		this.loginToken = loginToken;
		this.log = log;
	}

	/**
	 * This optionally creates a jira version
	 * @param jiraProjectKey The jira project key
	 * @param versions The verisons
	 * @param jiraVersion The jira version to create
	 * @return The created or existing version
	 * @throws RemoteException If an error occurred creating the version
	 * @throws java.rmi.RemoteException If an error occurred creating the version
	 */
	public RemoteVersion optionallyCreateVersion(String jiraProjectKey, RemoteVersion[] versions, String jiraVersion) throws RemoteException,
			java.rmi.RemoteException {
		RemoteVersion existing = getExistingVersion(versions, jiraVersion);
		if (existing == null) {
			RemoteVersion newVersion = new RemoteVersion();
			this.log.debug("New Version in JIRA is: " + jiraVersion);
			newVersion.setName(jiraVersion);
			RemoteVersion version = this.jiraService.addVersion(this.loginToken, jiraProjectKey, newVersion);
			this.log.info("Version created in JIRA for project key " + jiraProjectKey + " : " + jiraVersion);
			return version;
		} else {
			this.log.info(String.format("Version %s is already created in JIRA.", jiraVersion));
			return existing;
		}
	}

	/**
	 * This releases the jira version, creating it if not already existing
	 * @param jiraProjectKey The jira project key
	 * @param versions The jira versions
	 * @param jiraVersion The jira version to release
	 * @return The jira version
	 * @throws RemoteException
	 * @throws java.rmi.RemoteException
	 */
	public RemoteVersion releaseVersion(String jiraProjectKey, RemoteVersion[] versions, String jiraVersion) throws RemoteException, java.rmi.RemoteException {
		RemoteVersion releaseVersion = optionallyCreateVersion(jiraProjectKey, versions, jiraVersion);
		if (releaseVersion.isReleased()) {
			this.log.warn("The jira version: " + jiraVersion + " is already released, not releasing it");
		} else {
			// Mark as released
			releaseVersion.setReleased(true);
			releaseVersion.setReleaseDate(Calendar.getInstance());
			this.jiraService.releaseVersion(this.loginToken, jiraProjectKey, releaseVersion);
		}
		return releaseVersion;
	}

	/**
	 * This gets the existing version matching the given array of versions
	 * @param versions the versions to check
	 * @param version The version name to match against
	 * @return The matching version or null if none matched
	 */
	public RemoteVersion getExistingVersion(RemoteVersion[] versions, String version) {
		RemoteVersion existingVersion = null;
		if (versions != null) {
			// Creating new Version (if not already created)
			for (RemoteVersion remoteVersion : versions) {
				if (remoteVersion.getName().equalsIgnoreCase(version)) {
					existingVersion = remoteVersion;
					break;
				}
			}
		}
		return existingVersion;
	}

	/**
	 * This updates the fix version of any issues referencing the release version to instead reference the next version
	 * @param jiraService The jira web service
	 * @param jiraProjectKey The jira project key
	 * @param releaseVersion The jira version issues are being moved from
	 * @param nextVersion The jira version issues will be moved to
	 * @param maxIssuesToUpdate The max number of issues update
	 * @throws java.rmi.RemoteException
	 */
	public void updateFixVersions(JiraSoapService jiraService, String jiraProjectKey, RemoteVersion releaseVersion, RemoteVersion nextVersion,
			int maxIssuesToUpdate) throws java.rmi.RemoteException {

		this.log.info("Searching for issues to updated the fix version of from: " + releaseVersion.getName() + " to: " + nextVersion.getName());

		// find all issues against the old version and move to the new version
		int numIssuesUpdated = 0;
		String prevResultKeys = "";
		while (true && numIssuesUpdated < maxIssuesToUpdate) {
			String jqlSearch = "project='" + jiraProjectKey + "' and fixVersion='" + releaseVersion.getName() + "'";
			RemoteIssue[] issues = jiraService.getIssuesFromJqlSearch(this.loginToken, jqlSearch, ISSUES_PER_SEARCH);
			if (issues == null || issues.length == 0) {
				break;
			}

			// build a string representing the results to make sure we dont get the same results for consecutive calls which would
			// imply fix versions failed to update and avoid looping repeatedly
			StringBuilder resultsKey = new StringBuilder();
			for (RemoteIssue issue : issues) {
				resultsKey.append(issue.getKey());
			}
			if (resultsKey.toString().equalsIgnoreCase(prevResultKeys)) {
				this.log.warn("Failed to update fix versions, check that the account being used has permission to edit issue fixed versions!, skipping editing issues.");
				break;
			} else {
				prevResultKeys = resultsKey.toString();
			}

			for (RemoteIssue issue : issues) {
				// for each issue build up the new versions and update the issue
				RemoteVersion[] fixVersions = issue.getFixVersions();
				if (fixVersions == null || fixVersions.length == 0) {
					this.log.warn("Fix versions of jira: " + issue.getKey() + " was empty when it should have had the fix version: " + releaseVersion.getName());
				} else {

					RemoteFieldValue fixVersionFieldVal = new RemoteFieldValue();
					fixVersionFieldVal.setId("fixVersions");
					Set<String> versionVals = new HashSet<String>();
					for (RemoteVersion fixVersion : fixVersions) {
						if (fixVersion.getName().equalsIgnoreCase(releaseVersion.getName())) {
							// add on the next version thus replacing prev version with the next one
							versionVals.add(nextVersion.getId());
						} else {
							// add on the existing version
							versionVals.add(fixVersion.getId());
						}
					}
					fixVersionFieldVal.setValues(versionVals.toArray(new String[versionVals.size()]));
					this.log.info("Updating fix versions of jira: " + issue.getKey() + " to be: " + versionVals);
					// update the issue
					jiraService.updateIssue(this.loginToken, issue.getKey(), new RemoteFieldValue[] { fixVersionFieldVal });
				}
			}
			numIssuesUpdated += issues.length;
		}

		this.log.info("Updated the fix version of " + numIssuesUpdated + " issue(s) from: " + releaseVersion.getName() + " to: " + nextVersion.getName());

	}

	/**
	 * This optionally creates a jira version
	 * @param versionSpec The version specification
	 * @return The created or existing version
	 * @throws RemoteException If an error occurred creating the version
	 * @throws java.rmi.RemoteException If an error occurred creating the version
	 */
	public RemoteVersion optionallyCreateVersion(JiraVersionSpec versionSpec) throws RemoteException, java.rmi.RemoteException {
		String releaseJiraVersion = versionSpec.generateCurrentJiraVersion();
		RemoteVersion[] versions = this.jiraService.getVersions(this.loginToken, versionSpec.getJiraProjectKey());
		RemoteVersion version = optionallyCreateVersion(versionSpec.getJiraProjectKey(), versions, releaseJiraVersion);
		return version;
	}

	/**
	 * This will use the given version spec to release a jira version, it will create the existing version if it doesnt
	 * exist, release it, create the next version and then update existing issues that refer to the release jira version
	 * to the next version
	 * @param versionSpec The version release specification
	 * @throws java.rmi.RemoteException
	 */
	public void releaseVersion(JiraVersionSpec versionSpec) throws java.rmi.RemoteException {
		RemoteVersion[] versions = this.jiraService.getVersions(this.loginToken, versionSpec.getJiraProjectKey());

		// create if needed and then release the jira version corresponding to the jira version
		String releaseJiraVersion = versionSpec.generateCurrentJiraVersion();
		RemoteVersion releaseVersion = releaseVersion(versionSpec.getJiraProjectKey(), versions, releaseJiraVersion);

		// move any issues from the version just released to the next version
		String nextJiraVersion = versionSpec.generateNextJiraVersion();
		if (nextJiraVersion != null) {
			// create the next jira version and move any issues from the old version to the new one
			RemoteVersion nextVersion = optionallyCreateVersion(versionSpec.getJiraProjectKey(), versions, nextJiraVersion);
			if (nextVersion.isReleased()) {
				this.log.warn("The next jira version: " + nextVersion + " is already released, not moving issues to it");
			} else {

				if (versionSpec.isMoveIssuesToNextJiraVersion()) {
					updateFixVersions(this.jiraService, versionSpec.getJiraProjectKey(), releaseVersion, nextVersion, versionSpec.getMaxIssuesToUpdate());
				} else {
					this.log.info("Not updating the fix version of issues from: " + releaseVersion.getName() + " to: " + nextVersion.getName()
							+ " as the moveIssuesToNextJiraVersion setting is false.");
				}
			}

		} else {
			this.log.info("Not creating next jira version and moving any issues from prev version as no nextVersion was configured.");
		}
	}

}
