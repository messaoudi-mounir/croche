/*
 * Copyright © 2012 Avego Ltd., All Rights Reserved.
 * For licensing terms please contact Avego LTD.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package croche.maven.plugin.android.manifestv;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * The ManifestVersionMojo represents a maven mojo that will set properties
 * that can be used in an android manifest file
 * @version $Id$
 * @author conorroche
 * @goal set-version-props
 * @phase initialize
 * @requiresProject
 */
public class ManifestVersionMojo extends AbstractMojo {

	/**
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	MavenProject project;

	/**
	 * @parameter expression="${session}"
	 * @readonly
	 * @required
	 */
	protected MavenSession session;

	/**
	 * {@inheritDoc}
	 * @see org.apache.maven.plugin.Mojo#execute()
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		// get maven project version
		String currentVersion = this.project.getVersion();
		// manifest version name will be as follows:
		// 1. For a hudson snapshot build (where BUILD_NUMBER property is set) use currentVersion-BUILD_NUMBER
		// 2. For a release version (No snapshot in version number) use currentVersion
		// 3. For a non hudson build e.g. a developer local use currentVersion-Timestamp
		String manifestVersionName = "";
		String buildNumber = readProperty("BUILD_NUMBER");
		if (buildNumber.length() > 0) {
			manifestVersionName = currentVersion + "-" + buildNumber;
		} else if (currentVersion.toLowerCase().contains("snapshot")) {
			manifestVersionName = currentVersion + "-" + System.currentTimeMillis();
		} else {
			// its a release v and no hudson build number property available
			manifestVersionName = currentVersion;
		}
		String manifestVersionCode = buildVersionCode(manifestVersionName);

		getLog().info("Setting manifestVersionCode property to: " + manifestVersionCode);
		getLog().info("Setting manifestVersionName property to: " + manifestVersionName);
		setProperty("manifestVersionName", manifestVersionName);
		setProperty("manifestVersionCode", manifestVersionCode);
	}

	private String buildVersionCode(String versionName) {
		StringBuilder code = new StringBuilder();
		for (int i = 0; i < versionName.length(); i++) {
			char c = versionName.charAt(i);
			if (Character.isDigit(c)) {
				code.append(c);
			}
		}
		return code.toString();
	}

	private void setProperty(String name, String value) {
		System.setProperty(name, value);
		this.session.getExecutionProperties().setProperty(name, value);
	}

	private String readProperty(String name) {
		String mvnVal = this.session.getExecutionProperties().getProperty(name);
		if (mvnVal != null && mvnVal.trim().length() > 0) {
			return mvnVal.trim();
		}
		String sysPropVal = System.getProperty(name);
		if (sysPropVal != null && sysPropVal.trim().length() > 0) {
			return sysPropVal.trim();
		}
		String envVal = System.getenv(name);
		if (envVal != null && envVal.trim().length() > 0) {
			return envVal.trim();
		}
		return "";
	}

}
