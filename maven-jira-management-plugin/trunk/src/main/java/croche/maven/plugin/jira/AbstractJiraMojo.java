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

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.rpc.ServiceException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;

import com.atlassian.jira.rpc.soap.client.JiraSoapService;
import com.atlassian.jira.rpc.soap.client.JiraSoapServiceServiceLocator;

/**
 * The AbstractJiraMojo represents a base class for a maven mojo, adapted from
 * https://github.com/gastaldi/maven-jira-plugin
 * @version $Id$
 * @author gastaldi
 * @author conorroche
 */
public abstract class AbstractJiraMojo extends AbstractMojo {

	/**
	 * This is the JIRA SOAP Suffix for accessing the webservice
	 */
	protected static final String JIRA_SOAP_SUFFIX = "/rpc/soap/jirasoapservice-v2";

	/**
	 * @parameter expression="${settings}"
	 */
	Settings settings;

	/**
	 * Server's id in settings.xml to look up username and password.
	 * @parameter expression="${settingsKey}"
	 */
	private String settingsKey;

	/**
	 * JIRA Installation URL. If not informed, it will use the
	 * project.issueManagement.url info.
	 * @parameter expression="${jiraURL}" default-value="${project.issueManagement.url}"
	 * @required
	 */
	protected String jiraURL;

	/**
	 * JIRA Authentication User.
	 * @parameter expression="${jiraUser}" default-value="${scmUsername}"
	 */
	protected String jiraUser;

	/**
	 * JIRA Authentication Password.
	 * @parameter expression="${jiraPassword}" default-value="${scmPassword}"
	 */
	protected String jiraPassword;

	/**
	 * JIRA Project Key.
	 * @parameter expression="${jiraProjectKey}"
	 */
	protected String jiraProjectKey;

	transient JiraSoapService jiraService;

	/**
	 * Returns if this plugin is enabled for this context
	 * @parameter expression="${skip}"
	 */
	protected boolean skip;

	/**
	 * Returns the stub needed to invoke the WebService
	 * @return
	 * @throws MalformedURLException
	 * @throws ServiceException
	 */
	protected JiraSoapService getJiraSoapService() throws MalformedURLException, ServiceException {
		if (this.jiraService == null) {
			JiraSoapServiceServiceLocator locator = new JiraSoapServiceServiceLocator();
			String url = discoverJiraWSURL();
			if (url == null) {
				throw new MalformedURLException("JIRA URL cound not be found. Check your pom.xml configuration.");
			}
			URL u = new URL(url);
			this.jiraService = locator.getJirasoapserviceV2(u);
		}
		return this.jiraService;
	}

	/**
	 * Returns the formatted JIRA WebService URL
	 * @return JIRA Web Service URL
	 */
	String discoverJiraWSURL() {
		String url;
		if (this.jiraURL == null) {
			return null;
		}
		if (this.jiraURL.endsWith(JIRA_SOAP_SUFFIX)) {
			url = this.jiraURL;
		} else {
			int projectIdx = this.jiraURL.indexOf("/browse");
			if (projectIdx > -1) {
				int lastPath = this.jiraURL.indexOf("/", projectIdx + 8);
				if (lastPath == -1) {
					lastPath = this.jiraURL.length();
				}
				this.jiraProjectKey = this.jiraURL.substring(projectIdx + 8, lastPath);
				url = this.jiraURL.substring(0, projectIdx) + JIRA_SOAP_SUFFIX;
			} else {
				url = this.jiraURL + JIRA_SOAP_SUFFIX;
			}
		}
		return url;
	}

	/**
	 * Load username password from settings if user has not set them in JVM
	 * properties
	 */
	void loadUserInfoFromSettings() {
		if (this.settingsKey == null) {
			this.settingsKey = this.jiraURL;
		}
		if ((this.jiraUser == null || this.jiraPassword == null) && (this.settings != null)) {
			Server server = this.settings.getServer(this.settingsKey);

			if (server != null) {
				if (this.jiraUser == null) {
					this.jiraUser = server.getUsername();
				}

				if (this.jiraPassword == null) {
					this.jiraPassword = server.getPassword();
				}
			}
		}
	}

	public final void execute() throws MojoExecutionException, MojoFailureException {
		Log log = getLog();
		if (isSkip()) {
			log.info("Skipping Plugin execution.");
			return;
		}
		try {
			JiraSoapService jiraService = getJiraSoapService();
			loadUserInfoFromSettings();
			log.debug("Logging in JIRA");
			String loginToken = jiraService.login(this.jiraUser, this.jiraPassword);
			log.debug("Logged in JIRA");
			try {
				doExecute(jiraService, loginToken);
			} finally {
				log.debug("Logging out from JIRA");
				jiraService.logout(loginToken);
				log.debug("Logged out from JIRA");
			}
		} catch (Exception e) {
			log.error("Error when executing mojo", e);
		}
	}

	/**
	 * This executes the mojo passing in the jira server and login token
	 * @param jiraService The jira service
	 * @param loginToken The jira login token
	 * @throws Exception If an error occurs
	 */
	public abstract void doExecute(JiraSoapService jiraService, String loginToken) throws Exception;

	/**
	 * This gets the settingsKey
	 * @return the settingsKey
	 */
	public String getSettingsKey() {
		return this.settingsKey;
	}

	/**
	 * This sets the settingsKey
	 * @param settingsKey the settingsKey to set
	 */
	public void setSettingsKey(String settingsKey) {
		this.settingsKey = settingsKey;
	}

	/**
	 * This gets the jiraURL
	 * @return the jiraURL
	 */
	public String getJiraURL() {
		return this.jiraURL;
	}

	/**
	 * This sets the jiraURL
	 * @param jiraURL the jiraURL to set
	 */
	public void setJiraURL(String jiraURL) {
		this.jiraURL = jiraURL;
	}

	/**
	 * This gets the jiraUser
	 * @return the jiraUser
	 */
	public String getJiraUser() {
		return this.jiraUser;
	}

	/**
	 * This sets the jiraUser
	 * @param jiraUser the jiraUser to set
	 */
	public void setJiraUser(String jiraUser) {
		this.jiraUser = jiraUser;
	}

	/**
	 * This gets the jiraPassword
	 * @return the jiraPassword
	 */
	public String getJiraPassword() {
		return this.jiraPassword;
	}

	/**
	 * This sets the jiraPassword
	 * @param jiraPassword the jiraPassword to set
	 */
	public void setJiraPassword(String jiraPassword) {
		this.jiraPassword = jiraPassword;
	}

	/**
	 * This gets the jiraProjectKey
	 * @return the jiraProjectKey
	 */
	public String getJiraProjectKey() {
		return this.jiraProjectKey;
	}

	/**
	 * This sets the jiraProjectKey
	 * @param jiraProjectKey the jiraProjectKey to set
	 */
	public void setJiraProjectKey(String jiraProjectKey) {
		this.jiraProjectKey = jiraProjectKey;
	}

	/**
	 * This gets the skip
	 * @return the skip
	 */
	public boolean isSkip() {
		return this.skip;
	}

	/**
	 * This sets the skip
	 * @param skip the skip to set
	 */
	public void setSkip(boolean skip) {
		this.skip = skip;
	}

}
