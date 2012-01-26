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

/**
 * The JiraVersionSpec represents the specification of how to handle a jira version
 * @version $Id$
 * @author conorroche
 */
public class JiraVersionSpec {

	/**
	 * This is the keyword that means a group will have its integer value incremented
	 */
	public static final String INCREMENT = "INCREMENT";

	/**
	 * This is the keyword that means a group will have its value left as its group text
	 */
	public static final String GROUP_TEXT = "GROUP_TEXT";

	private static enum VERSION_TYPE {
		REGEX("regex"),
		SPRINT_Y3D("sprint_y3d"),
		V_2D("2d"),
		V_3D("3d"),
		V_3D_BRANCH_AWARE("3db");

		final String alias;

		/**
		 * This creates a VERSION_TYPE
		 * @param alias
		 */
		VERSION_TYPE(String alias) {
			this.alias = alias;
		}

		/**
		 * This finds a matching version type
		 * @param value The value
		 * @return The matching version type or null if none matched
		 */
		static VERSION_TYPE forValue(String value) {
			for (VERSION_TYPE type : VERSION_TYPE.values()) {
				if (type.alias.equalsIgnoreCase(value) || type.name().equalsIgnoreCase(value)) {
					return type;
				}
			}
			return null;
		}

	}

	/**
	 * JIRA Project Key.
	 * @parameter expression="${jiraProjectKey}"
	 */
	String jiraProjectKey;

	/**
	 * This is the optional version prefix to prefix the jira component version with
	 * @parameter expression="${jiraVersionPrefix}" default-value=""
	 */
	String jiraVersionPrefix = "";

	/**
	 * This is the existing version
	 * @parameter expression="${existingVersion}"
	 * @required
	 */
	String existingVersion;

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
	 * used after the release has been made, only applies for the version type of REGEX the nextVersionGroup is the number of the group in this
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
	Integer nextVersionGroup;
	/**
	 * This is the replacement string to use for the regex group identified by nextVersionGroup, this is optional
	 * and if NOT specified it defaults to INCREMENT which means it will increment the number identified by the nextVersionGroup.
	 * It may be set to GROUP_TEXT which means it will be set to the current value of the group and hence not replaced.
	 * @parameter expression="${nextVersionReplacement}" default-value="INCREMENT"
	 */
	String nextVersionReplacement;

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
	Integer maxIssuesToUpdate;

	/**
	 * This is the existing project version
	 * @return This is the existing project version
	 */
	public String getExistingVersion() {
		return this.existingVersion;
	}

	/**
	 * This gets the jiraProjectKey
	 * @return the jiraProjectKey
	 */
	public String getJiraProjectKey() {
		return this.jiraProjectKey;
	}

	/**
	 * This gets the jiraVersionPrefix
	 * @return the jiraVersionPrefix
	 */
	public String getJiraVersionPrefix() {
		return this.jiraVersionPrefix.replace("#space", " ");
	}

	/**
	 * This is the number of the group in the nextVersionRegex pattern that is the portion that is incremented/replaced with the nextVersionReplacement
	 * after release for the next development version. If the nextVersionRegex is specified then this will
	 * default to 1 if not specified.
	 * @return This is the number of the group in the nextVersionRegex pattern that is the portion that is incremented/replaced with the nextVersionReplacement
	 *         after release for the next development version. If the nextVersionRegex is specified then this will
	 *         default to 1 if not specified.
	 */
	public Integer getNextVersionGroup() {
		if (this.nextVersionGroup == null) {
			this.nextVersionGroup = 1;
		}
		return this.nextVersionGroup;
	}

	/**
	 * This is the regex pattern used for the next release e.g. the version number
	 * used after the release has been made, the nextVersionGroup is the number of the group in this
	 * pattern used as the portion that should be incremented/replaced with the nextVersionReplacement
	 * @return This is the regex pattern used for the development release e.g. the version number
	 *         used after the release has been made, the nextVersionGroup is the number of the group in this
	 *         pattern used as the portion that should be incremented/replaced with the nextVersionReplacement
	 */
	public String getNextVersionRegex() {
		return this.nextVersionRegex;
	}

	/**
	 * This is the replacement string to use for the regex group identified by nextVersionGroup, this is optional
	 * and if NOT specified it defaults to INCREMENT which means it will increment the number identified by the nextVersionGroup.
	 * It may be set to GROUP_TEXT which means it will be set to the current value of the group and hence not replaced.
	 * @return This is the replacement string to use for the regex group identified by nextVersionGroup, this is optional
	 *         and if NOT specified it defaults to INCREMENT which means it will increment the number identified by the nextVersionGroup.
	 *         It may be set to GROUP_TEXT which means it will be set to the current value of the group and hence not replaced.
	 */
	public String getNextVersionReplacement() {
		return this.nextVersionReplacement;
	}

	/**
	 * This sets the existingVersion
	 * @param existingVersion the existingVersion to set
	 */
	public void setExistingVersion(String existingVersion) {
		this.existingVersion = existingVersion;
	}

	/**
	 * This sets the jiraProjectKey
	 * @param jiraProjectKey the jiraProjectKey to set
	 */
	public void setJiraProjectKey(String jiraProjectKey) {
		this.jiraProjectKey = jiraProjectKey;
	}

	/**
	 * This sets the jiraVersionPrefix
	 * @param jiraVersionPrefix the jiraVersionPrefix to set
	 */
	public void setJiraVersionPrefix(String jiraVersionPrefix) {
		this.jiraVersionPrefix = jiraVersionPrefix;
	}

	/**
	 * This sets the nextVersionGroup
	 * @param nextVersionGroup the nextVersionGroup to set
	 */
	public void setNextVersionGroup(Integer nextVersionGroup) {
		this.nextVersionGroup = nextVersionGroup;
	}

	/**
	 * This sets the nextVersionRegex
	 * @param nextVersionRegex the nextVersionRegex to set
	 */
	public void setNextVersionRegex(String nextVersionRegex) {
		this.nextVersionRegex = nextVersionRegex;
	}

	/**
	 * This sets the nextVersionReplacement
	 * @param nextVersionReplacement the nextVersionReplacement to set
	 */
	public void setNextVersionReplacement(String nextVersionReplacement) {
		this.nextVersionReplacement = nextVersionReplacement;
	}

	/**
	 * This gets the moveIssuesToNextJiraVersion
	 * @return the moveIssuesToNextJiraVersion
	 */
	public boolean isMoveIssuesToNextJiraVersion() {
		return this.moveIssuesToNextJiraVersion;
	}

	/**
	 * This sets the moveIssuesToNextJiraVersion
	 * @param moveIssuesToNextJiraVersion the moveIssuesToNextJiraVersion to set
	 */
	public void setMoveIssuesToNextJiraVersion(boolean moveIssuesToNextJiraVersion) {
		this.moveIssuesToNextJiraVersion = moveIssuesToNextJiraVersion;
	}

	/**
	 * This gets the maxIssuesToUpdate
	 * @return the maxIssuesToUpdate
	 */
	public Integer getMaxIssuesToUpdate() {
		if (this.maxIssuesToUpdate == null) {
			this.maxIssuesToUpdate = 30;
		}
		return this.maxIssuesToUpdate;
	}

	/**
	 * This sets the maxIssuesToUpdate
	 * @param maxIssuesToUpdate the maxIssuesToUpdate to set
	 */
	public void setMaxIssuesToUpdate(Integer maxIssuesToUpdate) {
		this.maxIssuesToUpdate = maxIssuesToUpdate;
	}

	/**
	 * This gets the versionType
	 * @return the versionType
	 */
	public String getVersionType() {
		if (this.versionType == null) {
			this.versionType = "regex";
		}
		return this.versionType;
	}

	/**
	 * This sets the versionType
	 * @param versionType the versionType to set
	 */
	public void setVersionType(String versionType) {
		this.versionType = versionType;
	}

	/**
	 * This generates the jira version based on the existing project version and jira version prefix
	 * @return The jira version corresponding to the project version
	 */
	public String generateCurrentJiraVersion() {
		if (this.getExistingVersion() != null) {
			String jiraVersion = this.getJiraVersionPrefix() + this.getExistingVersion().replace("-SNAPSHOT", "");
			return jiraVersion;
		}
		return null;
	}

	/**
	 * This generates the next version for the existing version using the configured version type settings
	 * @param branch Whether the project version is a branch
	 * @return The next version string or null if there is no next version
	 */
	public String generateNextJiraVersion(boolean branch) {

		VERSION_TYPE versionType = VERSION_TYPE.forValue(getVersionType());
		if (versionType == null) {
			throw new IllegalArgumentException("Invalid version type: " + versionType);
		}
		NextVersionGenerator gen = null;
		switch (versionType) {
			case REGEX:
				String nextVersionRegex = getNextVersionRegex();
				if (nextVersionRegex != null && nextVersionRegex.length() > 0) {
					gen = new RegexNextVersionGenerator(nextVersionRegex, getNextVersionGroup(), this.nextVersionReplacement);
				}
				break;
			case SPRINT_Y3D:
				gen = new SprintNextVersionGenerator(branch);
				break;
			case V_2D:
				gen = new Increment2DNextVersionGenerator();
				break;
			case V_3D:
				gen = new Increment3DNextVersionGenerator();
				break;
			case V_3D_BRANCH_AWARE:
				gen = new Increment3DBNextVersionGenerator(branch);
				break;
			default:
				throw new IllegalArgumentException("Invalid version type: " + versionType);
		}

		if (gen != null) {
			String nextProjectVersion = gen.generateNextVersion(getExistingVersion());
			if (nextProjectVersion != null) {
				String nextJiraVersion = this.getJiraVersionPrefix() + nextProjectVersion.replace("-SNAPSHOT", "");
				return nextJiraVersion;
			}
		}
		return null;
	}

}
