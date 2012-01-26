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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The RegexNextVersionGenerator represents a next version generator that uses
 * a regex pattern to identify a group in the version to replace
 * @version $Id$
 * @author conorroche
 */
public class RegexNextVersionGenerator implements NextVersionGenerator {

	/**
	 * This is the keyword that means a group will have its integer value incremented
	 */
	public static final String INCREMENT = "INCREMENT";

	/**
	 * This is the keyword that means a group will have its value left as its group text
	 */
	public static final String GROUP_TEXT = "GROUP_TEXT";

	/**
	 * This is the regex pattern used for the next release e.g. the version number
	 * used after the release has been made, the nextVersionGroup is the number of the group in this
	 * pattern used as the portion that should be incremented/replaced with the nextVersionReplacement.
	 */
	private final String nextVersionRegex;
	/**
	 * This is the number of the group in the nextVersionRegex pattern that is the portion that is incremented/replaced with the nextVersionReplacement
	 * after release for the next development version. If the nextVersionRegex is specified then this will
	 * default to 1 if not specified.
	 */
	private final int nextVersionGroup;
	/**
	 * This is the replacement string to use for the regex group identified by nextVersionGroup, this is optional
	 * and if NOT specified it defaults to INCREMENT which means it will increment the number identified by the nextVersionGroup.
	 * It may be set to GROUP_TEXT which means it will be set to the current value of the group and hence not replaced.
	 */
	private final String nextVersionReplacement;

	/**
	 * This builds a new version from the given details, it will take the current version and then
	 * replace the regex group with the given number with the replacement, the replacement can be hardcoded text,
	 * or also one of two special values: 1) INCREMENT which means the number will be incremented, 2) GROUP_TEXT
	 * which means the replacement will be the group text
	 * @param currentVersion The current version
	 * @param group The number of the group in the regex expression
	 * @param regex The regular expression
	 * @param replacement The replacement value
	 * @return The new version string
	 */
	public static String replaceVersion(String currentVersion, int group, String regex, String replacement) {
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
						if (replacement.equals(INCREMENT)) {
							int nextVersion = -1;
							try {
								nextVersion = Integer.parseInt(groupText) + 1;
							} catch (NumberFormatException nfe) {
								throw new IllegalArgumentException("The group text: " + groupText + " matching the group: " + group
										+ " was not a valid integer and could not be incremented.");
							}
							sb.append(nextVersion);
						} else if (replacement.equals(GROUP_TEXT)) {
							sb.append(groupText);
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

	/**
	 * This creates a RegexNextVersionGenerator
	 * @param nextVersionRegex The regex pattern for the next version
	 * @param nextVersionGroup The group number in the pattern that should be replaced
	 * @param nextVersionReplacement The replacement value, can be INCREMENT meaning the number value in the group gets replaced,
	 *            GROUP_TEXT meaning the group value is left as is or it can be any other string
	 */
	public RegexNextVersionGenerator(String nextVersionRegex, int nextVersionGroup, String nextVersionReplacement) {
		super();
		if (nextVersionRegex == null || nextVersionRegex.length() == 0) {
			throw new IllegalArgumentException("The next version regex may not be null or empty");
		}
		if (nextVersionGroup < 1) {
			throw new IllegalArgumentException("The nextVersionGroup must be 1 or more");
		}

		String replacement = nextVersionReplacement;
		if (nextVersionReplacement == null || nextVersionReplacement.trim().length() == 0) {
			replacement = INCREMENT;
		}

		this.nextVersionRegex = nextVersionRegex.trim();
		this.nextVersionGroup = nextVersionGroup;
		this.nextVersionReplacement = replacement;
	}

	/**
	 * {@inheritDoc}
	 * @see croche.maven.plugin.jira.NextVersionGenerator#generateNextVersion(java.lang.String)
	 */
	public String generateNextVersion(String currentVersion) {
		return replaceVersion(currentVersion, this.nextVersionGroup, this.nextVersionRegex, this.nextVersionReplacement);
	}

}
