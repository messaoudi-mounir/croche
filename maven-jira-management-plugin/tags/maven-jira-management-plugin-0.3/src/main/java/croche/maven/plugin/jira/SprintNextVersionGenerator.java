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
 * The SprintNextVersionGenerator represents a next version generator
 * that generates the next version for sprint version of the form:
 * YYYY-QX.Y.X
 * @version $Id$
 * @author conorroche
 */
public class SprintNextVersionGenerator implements NextVersionGenerator {

	private final boolean incrementPatch;

	/**
	 * This creates a SprintNextVersionGenerator
	 * @param incrementPatch Whether to increment the patch number e.g. the third number
	 */
	public SprintNextVersionGenerator(boolean incrementPatch) {
		super();
		this.incrementPatch = incrementPatch;
	}

	/**
	 * {@inheritDoc}
	 * @see croche.maven.plugin.jira.NextVersionGenerator#generateNextVersion(java.lang.String)
	 */
	public String generateNextVersion(String currentVersion) {

		if (currentVersion != null) {
			SprintVersion sprint = new SprintVersion(currentVersion.replace("-SNAPSHOT", ""));
			SprintVersion next = sprint.nextVersion(this.incrementPatch);
			return next.toString();
		}

		return null;
	}

}
