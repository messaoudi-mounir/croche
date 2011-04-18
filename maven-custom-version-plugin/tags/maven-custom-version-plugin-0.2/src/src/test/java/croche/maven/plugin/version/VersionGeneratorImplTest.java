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

import junit.framework.TestCase;

/**
 * The VersionGeneratorImplTest represents a test case for the version generator impl
 * @version $Id$
 * @author conorroche
 */
public class VersionGeneratorImplTest extends TestCase {
	
	/**
	 * This tests the development version using different regex patterns 
	 */
	public void testDevelopmentVersion(){
		// first check if there is no regex for the version config that we get null for the dev version
		VersionGeneratorImpl versionGen = new VersionGeneratorImpl();
		String currentVersion = "1.1.0-SNAPSHOT";
		VersionConfig config = new VersionConfig();
		String developmentVersion = versionGen.generateDevelopmentVersion(config, currentVersion);
		assertNull(developmentVersion);
		// now test when we do have a regex
		config.devVersionRegex = ".*(\\d+)\\.(\\d+)\\.(\\d+).*";
		config.devVersionGroup = 2;
		developmentVersion = versionGen.generateDevelopmentVersion(config, currentVersion);
		assertEquals("1.2.0-SNAPSHOT", developmentVersion);
		// now test when we do have a regex
		config.devVersionRegex = ".*(\\d+)\\.(\\d+)\\.(\\d+).*";
		config.devVersionGroup = 3;
		developmentVersion = versionGen.generateDevelopmentVersion(config, currentVersion);
		assertEquals("1.1.1-SNAPSHOT", developmentVersion);
		
		// now test strings with different start and end values
		config.devVersionRegex = ".*(\\d+)\\.(\\d+)\\.(\\d+).*";
		config.devVersionGroup = 2;
		currentVersion = "xxxx1.1.0-SNAPSHOT";
		developmentVersion = versionGen.generateDevelopmentVersion(config, currentVersion);
		assertEquals("xxxx1.2.0-SNAPSHOT", developmentVersion);
		
		currentVersion = "xxxx1.1.0";
		developmentVersion = versionGen.generateDevelopmentVersion(config, currentVersion);
		assertEquals("xxxx1.2.0", developmentVersion);
		
		currentVersion = "xxxx1.1.0";
		config.devVersionGroup = 3;
		developmentVersion = versionGen.generateDevelopmentVersion(config, currentVersion);
		assertEquals("xxxx1.1.1", developmentVersion);
		
		currentVersion = "1.1.0";
		config.devVersionGroup = 1;
		developmentVersion = versionGen.generateDevelopmentVersion(config, currentVersion);
		assertEquals("2.1.0", developmentVersion);
		
		// now test a regex which uses diff separators between digits
		currentVersion = "xxxx1-1aaa0-XXX";
		config.devVersionRegex = ".*(\\d+)[^0-9]+(\\d+)[^0-9]+(\\d+).*";
		config.devVersionGroup = 3;
		developmentVersion = versionGen.generateDevelopmentVersion(config, currentVersion);
		assertEquals("xxxx1-1aaa1-XXX", developmentVersion);
		currentVersion = "1-1-0";
		developmentVersion = versionGen.generateDevelopmentVersion(config, currentVersion);
		assertEquals("1-1-1", developmentVersion);
		
		// now test a bad regex
	}
	
}
