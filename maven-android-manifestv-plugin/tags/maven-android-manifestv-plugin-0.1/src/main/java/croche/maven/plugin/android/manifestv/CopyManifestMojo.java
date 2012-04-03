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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * The CopyManifestMojo represents a maven mojo that will copy the manifest file from a source
 * path to a target path and apply property substitution to it
 * @version $Id$
 * @author conorroche
 * @goal copy-manifest
 * @phase initialize
 * @requiresProject
 */
public class CopyManifestMojo extends AbstractMojo {

	/**
	 * @parameter expression="${session}"
	 * @readonly
	 * @required
	 */
	protected MavenSession session;

	/**
	 * The path to the original manifest file, default value is ${project.basedir}/AndroidManifest.xml
	 * @parameter default-value="${project.basedir}/AndroidManifest.xml"
	 * @required
	 */
	protected String manifestPath;

	/**
	 * The path to copy the processed manifest file
	 * @parameter
	 * @required
	 */
	protected String targetPath;

	/**
	 * The encoding to use when copying the file, default is UTF-8
	 * @parameter default-value="UTF-8"
	 * @required
	 */
	protected String encoding;

	/**
	 * {@inheritDoc}
	 * @see org.apache.maven.plugin.Mojo#execute()
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {

		File sourceManifest = new File(this.manifestPath);
		if (!sourceManifest.canRead()) {
			throw new MojoFailureException("The manifest file: " + this.manifestPath + " set via the manifestPath configuration could not be read.");
		}
		File targetDir = new File(this.targetPath);
		if (!targetDir.canRead() && !targetDir.mkdirs()) {
			throw new MojoFailureException("The manifest targetPath: " + this.targetPath + " set via the targetPath configuration could not be read.");
		}

		File targetManifest = new File(targetDir, sourceManifest.getName());
		OutputStream os = null;
		try {
			os = new BufferedOutputStream(new FileOutputStream(targetManifest, false));
			LineIterator it = FileUtils.lineIterator(sourceManifest, this.encoding);
			Set<String> propNames = this.session.getExecutionProperties().stringPropertyNames();
			Properties props = this.session.getExecutionProperties();
			while (it.hasNext()) {
				String line = it.nextLine();
				// substitute any properies
				if (line.indexOf("${") > 0) {
					for (String propName : propNames) {
						line = line.replace("${" + propName + "}", props.getProperty(propName));
						if (line.indexOf("${") == -1) {
							break;
						}
					}
				}
				// write line out to output file
				os.write(line.toString().getBytes(this.encoding));
				os.write(IOUtils.LINE_SEPARATOR.getBytes(this.encoding));

			}
		} catch (IOException ex) {
			throw new MojoFailureException("Failed to iterate over the content of the manifest file: " + sourceManifest.getAbsolutePath(), ex);
		} finally {
			if (os != null) {
				try {
					os.flush();
				} catch (IOException ex) {
					throw new MojoFailureException("The target manifest targetManifest: " + targetManifest.getAbsolutePath()
							+ " could not written to due to an io error.", ex);
				} finally {
					IOUtils.closeQuietly(os);
				}
			}
		}

	}
}
