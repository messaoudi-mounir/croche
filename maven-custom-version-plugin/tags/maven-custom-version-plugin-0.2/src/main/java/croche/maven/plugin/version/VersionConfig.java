/*
 * Copyright 2011 Conor Roche
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
package croche.maven.plugin.version;

/**
 * The Version represents the POJO that holds the configuration details for
 * determining the release and development version of a release.
 * @version $Id$
 * @author conorroche
 */
public class VersionConfig {

	/**
	 * This is the keyword that means a group will have its integer value incremented
	 */
	public static final String INCREMENT = "INCREMENT";

	/**
	 * This is the keyword that means a group will have its value left as its group text
	 */
	public static final String GROUP_TEXT = "GROUP_TEXT";

	String devVersionRegex;
	Integer devVersionGroup;
	String devVersionReplacement;
	String devVersionType;

	String releaseVersionRegex;
	Integer releaseVersionGroup;
	String releaseVersionReplacement;

	/**
	 * This gets the devVersionType if not specified it defaults to regex but may be 3db meaning it is 3 number branch aware
	 * @return the devVersionType or regex if not specified
	 * @parameter
	 */
	public String getDevVersionType() {
		if (this.devVersionType == null || this.devVersionType.length() == 0) {
			this.devVersionType = "regex";
		}
		return this.devVersionType;
	}

	/**
	 * This is the regex pattern used for the development release e.g. the version number
	 * used after the release has been made, the devVersionGroup is the number of the group in this
	 * pattern used as the portion that should be incremented/replaced with the devVersionReplacement
	 * @return This is the regex pattern used for the development release e.g. the version number
	 *         used after the release has been made, the devVersionGroup is the number of the group in this
	 *         pattern used as the portion that should be incremented/replaced with the devVersionReplacement
	 * @parameter
	 */
	public String getDevVersionRegex() {
		if (this.devVersionRegex != null) {
			this.devVersionRegex = this.devVersionRegex.trim();
		}
		return this.devVersionRegex;
	}

	/**
	 * This is the number of the group in the devVersionRegex pattern that is the portion that is incremented/replaced with the devVersionReplacement
	 * after release for the next development version. If the devVersionRegex is specified then this will
	 * default to 1 if not specified.
	 * @return This is the number of the group in the devVersionRegex pattern that is the portion that is incremented/replaced with the devVersionReplacement
	 *         after release for the next development version. If the devVersionRegex is specified then this will
	 *         default to 1 if not specified.
	 * @parameter
	 */
	public Integer getDevVersionGroup() {
		if (this.devVersionGroup == null) {
			this.devVersionGroup = 1;
		}
		return this.devVersionGroup;
	}

	/**
	 * This sets the devVersionGroup
	 * @param devVersionGroup the devVersionGroup to set
	 */
	public void setDevVersionGroup(Integer devVersionGroup) {
		this.devVersionGroup = devVersionGroup;
	}

	/**
	 * This is the replacement string to use for the regex group identified by devVersionGroup, this is optional
	 * and if NOT specified it defaults to INCREMENT which means it will increment the number identified by the devVersionGroup.
	 * It may be set to GROUP_TEXT which means it will be set to the current value of the group and hence not replaced.
	 * @return This is the replacement string to use for the regex group identified by devVersionGroup, this is optional
	 *         and if NOT specified it defaults to INCREMENT which means it will increment the number identified by the devVersionGroup.
	 *         It may be set to GROUP_TEXT which means it will be set to the current value of the group and hence not replaced.
	 * @parameter
	 */
	public String getDevVersionReplacement() {
		if (this.devVersionReplacement == null || this.devVersionReplacement.trim().length() == 0) {
			this.devVersionReplacement = INCREMENT;
		}
		return this.devVersionReplacement;
	}

	/**
	 * This is the regex pattern used for the release e.g. the version number
	 * used for the release, the releaseVersionGroup is the number of the group in this
	 * pattern used as the portion that should be replaced with the releaseVersionReplacement
	 * @return This is the regex pattern used for the release e.g. the version number
	 *         used for the release, the releaseVersionGroup is the number of the group in this
	 *         pattern used as the portion that should be replaced with the releaseVersionReplacement
	 * @parameter
	 */
	public String getReleaseVersionRegex() {
		if (this.releaseVersionRegex != null) {
			this.releaseVersionRegex = this.releaseVersionRegex.trim();
		}
		return this.releaseVersionRegex;
	}

	/**
	 * This is the number of the group in the releaseVersionRegex pattern that is the portion that is replaced
	 * for the release with the releaseVersionReplacement. If the releaseVersionRegex is specified then this will
	 * default to 1 if not specified.
	 * @return This is the number of the group in the releaseVersionRegex pattern that is the portion that is replaced
	 *         for the release with the releaseVersionReplacement. If the releaseVersionRegex is specified then this will
	 *         default to 1 if not specified.
	 * @parameter
	 */
	public Integer getReleaseVersionGroup() {
		if (this.releaseVersionGroup == null) {
			this.releaseVersionGroup = 1;
		}
		return this.releaseVersionGroup;
	}

	/**
	 * This is the replacement string to use for the regex group identified by releaseVersionGroup, this is optional
	 * and if NOT specified it defaults to empty meaning it would get replaced with an empty string.
	 * It may be set to GROUP_TEXT which means it will be set to the current value of the group and hence not replaced.
	 * @return This is the replacement string to use for the regex group identified by releaseVersionGroup, this is optional
	 *         and if NOT specified it defaults to empty meaning it would get replaced with an empty string.
	 *         It may be set to GROUP_TEXT which means it will be set to the current value of the group and hence not replaced.
	 * @parameter
	 */
	public String getReleaseVersionReplacement() {
		if (this.releaseVersionReplacement == null) {
			this.releaseVersionReplacement = "";
		}
		return this.releaseVersionReplacement;
	}

	/**
	 * This sets the devVersionRegex
	 * @param devVersionRegex the devVersionRegex to set
	 */
	public void setDevVersionRegex(String devVersionRegex) {
		this.devVersionRegex = devVersionRegex;
	}

}
