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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The VersionGeneratorImpl represents a default implementation of a version generator that
 * supports regex to work out the versions. It sets them as the system properties that the Maven Release
 * Plugin uses for specifying the versions when releasing.
 * @version $Id$
 * @author conorroche
 */
public class VersionGeneratorImpl implements VersionGenerator {

	/**
	 * {@inheritDoc}
	 * @see croche.maven.plugin.version.VersionGenerator#generateDevelopmentVersion(croche.maven.plugin.version.VersionConfig, java.lang.String)
	 */
	public String generateDevelopmentVersion(VersionConfig config, String currentVersion) {
		if (config.getDevVersionRegex() != null && config.getDevVersionRegex().length() > 0) {
			if (config.getDevVersionGroup() < 1) {
				throw new IllegalArgumentException("The dev version group index must be >= 1");
			}
			return replaceVersion(currentVersion, config.getDevVersionGroup(), config.getDevVersionRegex(), config.getDevVersionReplacement());
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 * @see croche.maven.plugin.version.VersionGenerator#generateReleaseVersion(croche.maven.plugin.version.VersionConfig, java.lang.String)
	 */
	public String generateReleaseVersion(VersionConfig config, String currentVersion) {
		if (config.getReleaseVersionRegex() != null && config.getReleaseVersionRegex().length() > 0) {
			if (config.getReleaseVersionGroup() < 1) {
				throw new IllegalArgumentException("The release version group index must be >= 1");
			}
			return replaceVersion(currentVersion, config.getReleaseVersionGroup(), config.getReleaseVersionRegex(), config.getReleaseVersionReplacement());
		}
		return null;
	}

	private String replaceVersion(String currentVersion, int group, String regex, String replacement) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(currentVersion);
		StringBuilder sb = new StringBuilder();
		// int numGroups = matcher.groupCount();
		if (matcher.matches()) {
			int beginIndex = 0;
			int numGroups = matcher.groupCount();
			// check the required group is within range of the available groups
			if (group > numGroups) {
				throw new IllegalArgumentException("The group index must be between 1 and the number of groups in the regex pattern which is: " + numGroups);
			}
			int endIndex = -1;
			for (int i = 1; i <= numGroups; i++) {
				int groupStartIdx = matcher.start(i);
				int groupEndIdx = matcher.end(i);
				// if the group had some content
				if (groupStartIdx != groupEndIdx) {
					// add on the substring up to this group
					if (groupStartIdx > beginIndex) {
						sb.append(currentVersion.substring(beginIndex, groupStartIdx));
					}
					// add on the group content or the replacement if the group is the one to increment
					String groupText = matcher.group(i).trim();
					if (i == group) {
						// replace the group with the replacement text
						if(replacement.equals(VersionConfig.INCREMENT)){
							int nextVersion = -1;
							try {
								nextVersion = Integer.parseInt(groupText) + 1;
							} catch (NumberFormatException nfe) {
								throw new IllegalArgumentException("The group text: " + groupText + " matching the group: " + group
										+ " was not a valid integer and could not be incremented.");
							}
							sb.append(nextVersion);
						} else {
							sb.append(replacement);
						}
						
					} else {
						sb.append(groupText);
					}
				}
				beginIndex = groupEndIdx;
				endIndex = groupEndIdx;
			}
			// finally add on the remaining text
			if (endIndex > -1 && endIndex < currentVersion.length()) {
				sb.append(currentVersion.substring(endIndex));
			}
			return sb.toString();
		}
		throw new IllegalArgumentException("The current version: " + currentVersion + " did not match the regex pattern: " + regex);
	}

}
