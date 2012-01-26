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

import junit.framework.TestCase;

/**
 * The SprintNextVersionGeneratorTest represents
 * @version $Id$
 * @author conorroche
 */
public class SprintNextVersionGeneratorTest extends TestCase {

	/**
	 * This tests version generation
	 */
	public void testVersionGeneration() {

		NextVersionGenerator gen = new SprintNextVersionGenerator(true);
		String currentVersion = "2012-Q2.1.0";
		String nextVersion = gen.generateNextVersion(currentVersion);
		assertEquals("2012-Q2.1.1", nextVersion);

		currentVersion = "2012-Q4.1.4";
		nextVersion = gen.generateNextVersion(currentVersion);
		assertEquals("2012-Q4.1.5", nextVersion);

		currentVersion = "2012-Q2.1.1-SNAPSHOT";
		nextVersion = gen.generateNextVersion(currentVersion);
		assertEquals("2012-Q2.1.2", nextVersion);
		currentVersion = "2012-Q2.1";
		nextVersion = gen.generateNextVersion(currentVersion);
		assertEquals("2012-Q2.1.1", nextVersion);

		gen = new SprintNextVersionGenerator(false);
		currentVersion = "2012-Q2.1.0";
		nextVersion = gen.generateNextVersion(currentVersion);
		assertEquals("2012-Q2.2.0", nextVersion);
		currentVersion = "2012-Q2.1.0-SNAPSHOT";
		nextVersion = gen.generateNextVersion(currentVersion);
		assertEquals("2012-Q2.2.0", nextVersion);
		currentVersion = "2012-Q4.4.0";
		nextVersion = gen.generateNextVersion(currentVersion);
		assertEquals("2013-Q1.1.0", nextVersion);

	}

}
