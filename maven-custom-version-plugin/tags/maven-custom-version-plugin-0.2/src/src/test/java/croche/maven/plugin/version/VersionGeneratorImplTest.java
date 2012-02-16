/*
 * Copyright © 2011 Avego Ltd., All Rights Reserved.
 * For licensing terms please contact Avego LTD.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package croche.maven.plugin.version;

import java.util.Arrays;

import junit.framework.TestCase;

/**
 * The VersionGeneratorImplTest represents a test case for the version generator impl
 * @version $Id$
 * @author conorroche
 */
public class VersionGeneratorImplTest extends TestCase {

	/**
	 * This tests the development version using different regex patterns
	 * @throws InvalidVersionException
	 */
	public void testDevelopmentVersion() throws InvalidVersionException {
		// first check if there is no regex for the version config that we get null for the dev version
		VersionGeneratorImpl versionGen = new VersionGeneratorImpl();
		String currentVersion = "1.1.0-SNAPSHOT";
		VersionConfig config = new VersionConfig();
		String developmentVersion = versionGen.generateDevelopmentVersion(config, currentVersion, false);
		assertNull(developmentVersion);
		// now test when we do have a regex
		config.devVersionRegex = ".*(\\d+)\\.(\\d+)\\.(\\d+).*";
		config.devVersionGroup = 2;
		developmentVersion = versionGen.generateDevelopmentVersion(config, currentVersion, false);
		assertEquals("1.2.0-SNAPSHOT", developmentVersion);
		// now test when we do have a regex
		config.devVersionRegex = ".*(\\d+)\\.(\\d+)\\.(\\d+).*";
		config.devVersionGroup = 3;
		developmentVersion = versionGen.generateDevelopmentVersion(config, currentVersion, false);
		assertEquals("1.1.1-SNAPSHOT", developmentVersion);

		// now test strings with different start and end values
		config.devVersionRegex = ".*(\\d+)\\.(\\d+)\\.(\\d+).*";
		config.devVersionGroup = 2;
		currentVersion = "xxxx1.1.0-SNAPSHOT";
		developmentVersion = versionGen.generateDevelopmentVersion(config, currentVersion, false);
		assertEquals("xxxx1.2.0-SNAPSHOT", developmentVersion);

		currentVersion = "xxxx1.1.0";
		developmentVersion = versionGen.generateDevelopmentVersion(config, currentVersion, false);
		assertEquals("xxxx1.2.0", developmentVersion);

		currentVersion = "xxxx1.1.0";
		config.devVersionGroup = 3;
		developmentVersion = versionGen.generateDevelopmentVersion(config, currentVersion, false);
		assertEquals("xxxx1.1.1", developmentVersion);

		currentVersion = "1.1.0";
		config.devVersionGroup = 1;
		developmentVersion = versionGen.generateDevelopmentVersion(config, currentVersion, false);
		assertEquals("2.1.0", developmentVersion);

		// now test a regex which uses diff separators between digits
		currentVersion = "xxxx1-1aaa0-XXX";
		config.devVersionRegex = ".*(\\d+)[^0-9]+(\\d+)[^0-9]+(\\d+).*";
		config.devVersionGroup = 3;
		developmentVersion = versionGen.generateDevelopmentVersion(config, currentVersion, false);
		assertEquals("xxxx1-1aaa1-XXX", developmentVersion);
		currentVersion = "1-1-0";
		developmentVersion = versionGen.generateDevelopmentVersion(config, currentVersion, false);
		assertEquals("1-1-1", developmentVersion);

		// check invalid version for the regex
		currentVersion = "abc";
		try {
			developmentVersion = versionGen.generateDevelopmentVersion(config, currentVersion, false);
			fail("Expected invalid version");
		} catch (InvalidVersionException ive) {
			// we expect this
		}

		// test the 3db config works for trunk and branch
		currentVersion = "1.1.0";
		config.devVersionGroup = null;
		config.devVersionRegex = null;
		config.devVersionType = "3db";
		developmentVersion = versionGen.generateDevelopmentVersion(config, currentVersion, false);
		assertEquals("1.2.0", developmentVersion);
		currentVersion = "1.1.1";
		developmentVersion = versionGen.generateDevelopmentVersion(config, currentVersion, true);
		assertEquals("1.1.2", developmentVersion);

		// now test that if we use a branch v on a trunk it fails
		currentVersion = "1.1.1";
		try {
			developmentVersion = versionGen.generateDevelopmentVersion(config, currentVersion, false);
			fail("Expected invalid version");
		} catch (InvalidVersionException ive) {
			// we expect this
		}
		// now test that if we use a trunk v on a branch it fails
		currentVersion = "1.1.0";
		try {
			developmentVersion = versionGen.generateDevelopmentVersion(config, currentVersion, true);
			fail("Expected invalid version");
		} catch (InvalidVersionException ive) {
			// we expect this
		}
	}

	/**
	 * This tests the get version parts method
	 * @throws InvalidVersionException
	 */
	public void testGetVersionParts() throws InvalidVersionException {
		VersionGeneratorImpl versionGen = new VersionGeneratorImpl();
		String currentVersion = "1.1.0";
		String regex = ".*(\\d+)\\.(\\d+)\\.(\\d+).*";
		int[] expected = new int[] { 1, 1, 0 };
		int[] parts = versionGen.getVersionParts(currentVersion, regex);
		assertTrue(Arrays.equals(expected, parts));
		currentVersion = "1.1";
		regex = ".*(\\d+)\\.(\\d+).*";
		expected = new int[] { 1, 1 };
		parts = versionGen.getVersionParts(currentVersion, regex);
		assertTrue(Arrays.equals(expected, parts));
	}

}
