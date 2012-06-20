/*
 * Copyright © 2012 Avego Ltd., All Rights Reserved.
 * For licensing terms please contact Avego LTD.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package croche.maven.plugin.dbupgrade;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * The CreateUpgradeScriptMojo represents a mojo that builds the merged db upgrade script
 * @goal create-upgrade-scripts
 * @phase process-resources
 * @requiresProject
 * @version $Id$
 * @author conorroche
 */
public class CreateUpgradeScriptMojo extends AbstractMojo {

	/**
	 * The source directory where it will scan for files, it will look in this directory and all of its subdirectories
	 * @parameter
	 * @required
	 */
	protected File sourceDir;

	/**
	 * The target dir that the files created files will be written to
	 * @parameter
	 * @required
	 */
	protected File targetDir;

	/**
	 * This is include pattern for files to include
	 * @parameter
	 */
	protected String[] includes;

	/**
	 * This is exclude pattern for files
	 * @parameter
	 */
	protected String[] excludes;

	/**
	 * <pre>
	 * This is an optional separator output above merged files in the combined file.
	 * In the separator 3 variables are supported: 
	 * 1. \n will translate into a line break in the output file, 
	 * 2. #{file.name} will be replaced with the name of the file that is being appended into the target file, 
	 * 3. #{parent.name} will be replaced with the directory name containing the file that is being appended into
	 * the target file.
	 * </pre>
	 * @parameter
	 */
	protected String separator;

	/**
	 * This is an optional encoding to use when reading/writing the files being merged, if not specified
	 * then UTF-8 will be used
	 * @parameter default-value="UTF-8"
	 */
	protected String encoding;

	/**
	 * This is the names of the directories of the core specific projects
	 * @parameter
	 * @required
	 */
	protected String[] coreDirs;

	/**
	 * This is the names of the directories of the www specific projects
	 * @parameter
	 * @required
	 */
	protected String[] wwwDirs;

	/**
	 * This is the file name used for the generated sql file that contains all the sql files
	 * @parameter default-value="upgrade-all.sql"
	 */
	protected String allInOneFileName;

	/**
	 * This is the file name used for the generated sql file that contains the www sql files
	 * @parameter default-value="upgrade-www.sql"
	 */
	protected String wwwFileName;

	/**
	 * This is the file name used for the generated sql file that contains the core sql files
	 * @parameter default-value="upgrade-core.sql"
	 */
	protected String coreFileName;

	/**
	 * Gets the comma separated list of effective include patterns.
	 * @return The comma separated list of effective include patterns, never <code>null</code>.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	String getIncludesCSV() {
		Collection patterns = new LinkedHashSet();
		if (this.includes != null) {
			patterns.addAll(Arrays.asList(this.includes));
		}
		if (patterns.isEmpty()) {
			patterns.add("**/*");
		}
		return StringUtils.join(patterns.iterator(), ",");
	}

	/**
	 * Gets the comma separated list of effective exclude patterns.
	 * @return The comma separated list of effective exclude patterns, never <code>null</code>.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	String getExcludesCSV() {
		Collection patterns = new LinkedHashSet(FileUtils.getDefaultExcludesAsList());
		// add on the target files as excludes
		patterns.addAll(Arrays.asList(this.allInOneFileName, this.coreFileName, this.wwwFileName));
		if (this.excludes != null) {
			patterns.addAll(Arrays.asList(this.excludes));
		}
		return StringUtils.join(patterns.iterator(), ",");
	}

	static class Sprint {

		String version;
		List<File> wwwFiles = new ArrayList<File>();
		List<File> coreFiles = new ArrayList<File>();
		List<File> allInOneFiles = new ArrayList<File>();

	}

	private Map<String, Sprint> sprints = new HashMap<String, Sprint>();

	/**
	 * {@inheritDoc}
	 * @see org.apache.maven.plugin.Mojo#execute()
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {

		// scan the directories to build the total number of sprints for which there are upgrade scripts
		buildSprintData();

		// now create the files for all in one, www and core for each sprint
		for (Sprint sprint : this.sprints.values()) {
			try {
				createSprintFiles(sprint);
			} catch (IOException ex) {
				throw new MojoExecutionException("Failed to create the sprint files for the sprint: " + sprint, ex);
			}
		}
	}

	void createSprintFiles(Sprint sprint) throws IOException, MojoExecutionException {
		// remove existing target dir if it exists
		File sprintDir = new File(this.targetDir + File.separator + sprint.version);
		if (!sprintDir.exists()) {
			sprintDir.mkdir();
		}
		FileUtils.cleanDirectory(sprintDir);

		// build the merged all in one sql file
		mergeFiles(sprint.allInOneFiles, new File(sprintDir, this.allInOneFileName));

		// then the www one
		mergeFiles(sprint.wwwFiles, new File(sprintDir, this.wwwFileName));

		// finally the core one
		mergeFiles(sprint.coreFiles, new File(sprintDir, this.coreFileName));

		// copy all the files into the sprint dir
		for (File file : sprint.allInOneFiles) {
			FileUtils.copyFile(file, new File(sprintDir, file.getParentFile().getParentFile().getName() + ".sql"));
		}
	}

	private int mergeFiles(List<File> files, File targetFile) throws MojoExecutionException {

		int numMergedFiles = 0;
		// now append the files that have been found in the order required
		Writer ostream = null;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(targetFile, true);
			ostream = new OutputStreamWriter(fos, this.encoding);
			BufferedWriter output = new BufferedWriter(ostream);

			getLog().info("Appending: " + files.size() + " files to the target file: " + targetFile.getAbsolutePath() + "...");
			for (File file : files) {
				String fileName = file.getName();
				getLog().info("Appending file: " + file.getAbsolutePath() + " to the target file: " + targetFile.getAbsolutePath() + "...");
				InputStream input = null;
				try {
					input = new FileInputStream(file);

					// add separator if present
					if (this.separator != null && this.separator.trim().length() > 0) {
						String replaced = this.separator.trim();
						// remove any line breaks and tabs due to xml formatting
						replaced = replaced.replace("\n", "");
						replaced = replaced.replace("\t", "");
						// replace the file name and parent name variables
						replaced = replaced.replace("#{file.name}", fileName);
						replaced = replaced.replace("#{parent.name}", file.getParentFile() != null ? file.getParentFile().getName() : "");
						replaced = replaced.replace("#{grandparent.name}",
								(file.getParentFile() != null && file.getParentFile().getParentFile() != null) ? file.getParentFile().getParentFile().getName()
										: "");
						// add in any requested line breaks and tabs
						replaced = replaced.replace("\\n", "\n");
						replaced = replaced.replace("\\t", "\t");
						getLog().debug("Appending separator: " + replaced);
						IOUtils.copy(new StringReader(replaced), output);
					}
					// add file contents
					IOUtils.copy(input, output, this.encoding);
				} catch (IOException ioe) {
					throw new MojoExecutionException("Failed to append file: " + fileName + " to output file", ioe);
				} finally {
					IOUtils.closeQuietly(input);
				}
				numMergedFiles++;
			}

			output.flush();
		} catch (IOException ioe) {
			throw new MojoExecutionException("Failed to open stream file to output file: " + targetFile.getAbsolutePath(), ioe);
		} finally {
			if (fos != null) {
				IOUtils.closeQuietly(fos);
			}
			if (ostream != null) {
				IOUtils.closeQuietly(ostream);
			}
		}
		return numMergedFiles;
	}

	@SuppressWarnings("unchecked")
	void buildSprintData() throws MojoExecutionException {
		getLog().info("Scanning source directory: " + this.sourceDir.getAbsolutePath() + " for db upgrade sql scripts...");

		String including = getIncludesCSV();
		String excluding = getExcludesCSV();

		// first find matching files
		List<File> matchingFiles;
		try {
			matchingFiles = FileUtils.getFiles(this.sourceDir, including, excluding);
		} catch (IOException ioe) {
			throw new MojoExecutionException("Failed to find matching files of the source dir: " + this.sourceDir.getAbsolutePath(), ioe);
		}

		int numFiles = matchingFiles == null ? 0 : matchingFiles.size();
		getLog().info("Sourced directory: " + this.sourceDir.getAbsolutePath() + " contains " + numFiles + " upgrade scripts.");

		if (matchingFiles != null) {
			for (File file : matchingFiles) {
				// see what version it is if any
				try {
					SprintVersion version = new SprintVersion(getFileNameNoExt(file.getName()));
					String versionName = version.toString();
					Sprint sprint = this.sprints.get(versionName);
					if (sprint == null) {
						sprint = new Sprint();
						sprint.version = versionName;
						this.sprints.put(versionName, sprint);
					}

					// add to the all inone and core or www list
					sprint.allInOneFiles.add(file);
					if (isWwwFile(file)) {
						sprint.wwwFiles.add(file);
					} else if (isCoreFile(file)) {
						sprint.coreFiles.add(file);
					} else {
						throw new MojoExecutionException("The file: " + file.getAbsolutePath() + " did not match the www or core files");
					}

				} catch (SprintVersionException ex) {
					getLog().warn("Skipping file: " + file.getAbsolutePath() + " as it does not match a sprint version.");
				}
			}
		}

	}

	private boolean isWwwFile(File file) {
		if (this.wwwDirs != null) {
			for (String dir : this.wwwDirs) {
				if (file.getAbsolutePath().contains(dir)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isCoreFile(File file) {
		if (this.coreDirs != null) {
			for (String dir : this.coreDirs) {
				if (file.getAbsolutePath().contains(dir)) {
					return true;
				}
			}
		}
		return false;
	}

	private String getFileNameNoExt(String fileName) {
		int lastDotPos = fileName.lastIndexOf(".");
		return fileName.substring(0, lastDotPos);
	}

}
