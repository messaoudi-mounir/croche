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
 * The NextVersionGenerator represents a generator that can generate a new version given an existing version
 * @version $Id$
 * @author conorroche
 */
public interface NextVersionGenerator {

	/**
	 * This generates the next version for the given version
	 * @param currentVersion The current version
	 * @return The next version
	 */
	public String generateNextVersion(String currentVersion);

}
