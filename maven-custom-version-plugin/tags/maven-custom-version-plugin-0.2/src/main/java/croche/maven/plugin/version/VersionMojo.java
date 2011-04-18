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

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * The VersionMojo represents a mojo that can set the version properties to pass into the Maven Release Plugin
 * by a custom configuration. For example you can pass it a regex for the next development version which it will
 * then use to generate the next development version for a given release.
 * By default it runs at the initialization stage, reads the project pom and the version configuration
 * and uses that to generate the next release and or development versions
 * @version $Id$
 * @author conorroche
 * @goal generate-versions
 * @phase initialize
 * @requiresProject
 */
public class VersionMojo extends AbstractMojo {

	/**
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	MavenProject project;
	
	/**
	 * @parameter
	 * @required
	 */
	VersionConfig versionConfig;
	
	/**
     * @parameter expression="${session}"
     * @readonly
     * @required
     * @since 2.0
     */
    protected MavenSession session;
	
	/** 
	 * {@inheritDoc}
	 * @see org.apache.maven.plugin.Mojo#execute()
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		// get the current version of the artifact being released
		String currentVersion = this.project.getVersion();
		// get the version generator
		VersionGenerator versionGen = VersionGeneratorFactory.createVersionGenerator();
		String releaseVersion = versionGen.generateReleaseVersion(this.versionConfig, currentVersion);
		// set the system property for the release plugin
		if(releaseVersion != null){
			getLog().info("Setting sys & exec env property releaseVersion to: " + releaseVersion);
			System.setProperty("releaseVersion", releaseVersion);
			this.session.getExecutionProperties().setProperty("releaseVersion", releaseVersion);
		} else {
			getLog().info("Not setting releaseVersion.");
		}
		String developmentVersion = versionGen.generateDevelopmentVersion(this.versionConfig, currentVersion);
		// set the system property for the release plugin
		if(developmentVersion != null){
			getLog().info("Setting sys & exec env property developmentVersion to: " + developmentVersion);
			System.setProperty("developmentVersion", developmentVersion);
			this.session.getExecutionProperties().setProperty("developmentVersion", developmentVersion);
		} else {
			getLog().info("Not setting developmentVersion.");
		}
	}
	
}
