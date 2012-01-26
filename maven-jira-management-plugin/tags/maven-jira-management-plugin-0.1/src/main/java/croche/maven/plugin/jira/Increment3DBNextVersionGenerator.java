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
 * The Increment3DBNextVersionGenerator represents a version generator that will increment the 3rd number in a 3 number version string
 * if its a branch otherwise it will increment the 2nd number
 * @version $Id$
 * @author conorroche
 */
public class Increment3DBNextVersionGenerator implements NextVersionGenerator {

	final boolean branch;

	/**
	 * This creates a Increment3DBNextVersionGenerator
	 * @param branch Whether this is a branch if so the 3d digit will be incremented
	 */
	public Increment3DBNextVersionGenerator(boolean branch) {
		super();
		this.branch = branch;
	}

	/**
	 * {@inheritDoc}
	 * @see croche.maven.plugin.jira.NextVersionGenerator#generateNextVersion(java.lang.String)
	 */
	public String generateNextVersion(String currentVersion) {
		int groupNum = this.branch ? 3 : 2;
		RegexNextVersionGenerator gen = new RegexNextVersionGenerator(".*(\\d+)[^0-9]+(\\d+)[^0-9]+(\\d+).*", groupNum, RegexNextVersionGenerator.INCREMENT);
		return gen.generateNextVersion(currentVersion);
	}

}
