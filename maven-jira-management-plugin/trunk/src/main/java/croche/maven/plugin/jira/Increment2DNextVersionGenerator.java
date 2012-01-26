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
 * The Increment2DNextVersionGenerator represents a next version generator that expects
 * version to be in the form X.Y and will increment Y as the next version, NOTE
 * the 2 numbers can start with any value so for example 2.1, 2-1, 21, R2.1, 2.1GA would all be as is except
 * 1 would become 2.
 * @version $Id$
 * @author conorroche
 */
public class Increment2DNextVersionGenerator implements NextVersionGenerator {

	/**
	 * {@inheritDoc}
	 * @see croche.maven.plugin.jira.NextVersionGenerator#generateNextVersion(java.lang.String)
	 */
	public String generateNextVersion(String currentVersion) {
		RegexNextVersionGenerator gen = new RegexNextVersionGenerator(".*(\\d+)[^0-9]+(\\d+).*", 2, RegexNextVersionGenerator.INCREMENT);
		return gen.generateNextVersion(currentVersion);
	}

}
