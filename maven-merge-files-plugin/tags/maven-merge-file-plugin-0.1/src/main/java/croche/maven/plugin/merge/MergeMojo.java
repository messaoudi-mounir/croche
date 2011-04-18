/*
 * Copyright 20100 Conor Roche
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
package croche.maven.plugin.merge;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Goal which merges text files from a series of directories into a single text output file
 * @goal merge
 * @phase process-resources
 * @requiresProject
 */
public class MergeMojo extends AbstractMojo {

	private static final String DEFAULT_ENCODING = "UTF-8";
	private static final String DEFAULT_ORDERING_NAME = "MergeMojoDefaultOrdering";
	private static final List<String> DEFAULT_ORDERING = new ArrayList<String>(1);
	static {
		DEFAULT_ORDERING.add(DEFAULT_ORDERING_NAME);
	}

	/**
	 * The properties files to merge. <br>
	 * Usage:
	 * 
	 * <pre>
	 * &lt;merges&gt;
	 * &nbsp;&nbsp;&lt;merge&gt;
	 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;targetFile&gt;${build.outputDirectory}/merged.sql&lt;/targetFile&gt;
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;sourceDirs&gt;
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;sourceDir&gt;src/main/sql/app1&lt;/sourceDir&gt;
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;sourceDir&gt;src/main/sql/app2&lt;/sourceDir&gt;
	 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;/sourceDirs&gt;
	 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;nameContainsOrderings&gt;
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;nameContainsOrdering&gt;create-schema&lt;/nameContainsOrdering&gt;
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;nameContainsOrdering&gt;schema-objects&lt;/nameContainsOrdering&gt;
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;nameContainsOrdering&gt;indices&lt;/nameContainsOrdering&gt;
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;nameContainsOrdering&gt;data&lt;/nameContainsOrdering&gt;
	 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;/nameContainsOrderings&gt;
	 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;includes&gt;
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;include&gt;.sql&lt;/include&gt;
	 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;/includes&gt;
	 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;separator&gt;--- ${filename} ---&lt;/separator&gt;
	 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;encoding&gt;UTF-8&lt;/encoding&gt;
	 * &nbsp;&nbsp;&lt;/merge&gt;
	 * &lt;/merges&gt;
	 * </pre>
	 * @parameter
	 * @required
	 */
	private Merge[] merges;

	private Map<String, List<File>> orderedFiles;
	private boolean useOrdering;
	private List<String> orderingNames;

	/**
	 * @see org.apache.maven.plugin.AbstractMojo#execute()
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		for (Merge merge : this.merges) {

			// remove target file if present...
			if (merge.getTargetFile().exists()) {
				merge.getTargetFile().delete();
			}

			// build ordering names
			buildOrderings(merge);

			// scan directories to build the ordered set of files to be written
			scanDirectories(merge);

			// merge the files into the target directory
			int numMergedFiles = mergeFiles(merge);

			getLog().info("Finished Appending: " + numMergedFiles + " files to the target file: " + merge.getTargetFile().getAbsolutePath() + ".");

		}
	}

	private int mergeFiles(Merge merge) throws MojoExecutionException {

		// read the encoding to use
		String encoding = DEFAULT_ENCODING;
		if (merge.getEncoding() != null && merge.getEncoding().length() > 0) {
			encoding = merge.getEncoding();
		}

		int numMergedFiles = 0;
		// now append the files that have been found in the order required
		Writer ostream = null;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(merge.getTargetFile(), true);
			ostream = new OutputStreamWriter(fos, encoding);
			BufferedWriter output = new BufferedWriter(ostream);
			for (String orderingName : this.orderingNames) {
				List<File> files = this.orderedFiles.get(orderingName);
				if (files != null) {
					getLog().info(
							"Appending: " + files.size() + " files that matched the name: " + orderingName + " to the target file: "
									+ merge.getTargetFile().getAbsolutePath() + "...");
					for (File file : files) {
						String fileName = file.getName();
						getLog().info("Appending file: " + fileName + " to the target file: " + merge.getTargetFile().getAbsolutePath() + "...");
						InputStream input = null;
						try {
							input = new FileInputStream(file);

							// add separator if present
							if (merge.getSeparator() != null && merge.getSeparator().length() > 0) {
								// #{parent.name}/#{file.name}
								String replaced = merge.getSeparator().replace("#{file.name}", fileName);
								replaced = replaced.replace("#{parent.name}", file.getParentFile() != null ? file.getParentFile().getName() : "");
								replaced = replaced.replace("\\n", "\n");
								getLog().debug("Appending separator: " + replaced);
								IOUtils.copy(new StringReader(replaced), output);
							}
							// add file contents
							IOUtils.copy(input, output, encoding);
						} catch (IOException ioe) {
							throw new MojoExecutionException("Failed to append file: " + fileName + " to output file", ioe);
						} finally {
							IOUtils.closeQuietly(input);
						}
						numMergedFiles++;
					}
				}
			}
			output.flush();
		} catch (IOException ioe) {
			throw new MojoExecutionException("Failed to open stream file to output file: " + merge.getTargetFile().getAbsolutePath(), ioe);
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

	/**
	 * This scans the configured source directories and builds uo the map of ordered
	 * files
	 * @param merge
	 */
	private void scanDirectories(Merge merge) {
		this.orderedFiles = new HashMap<String, List<File>>(this.orderingNames.size());

		// find all the files that are in the source directories and add to the appropriate list based on the file name
		File[] sourceDirs = merge.getSourceDirs();
		for (File sourceDir : sourceDirs) {
			if (!sourceDir.exists()) {
				getLog().warn("The source directory: " + sourceDir.getAbsolutePath() + " did not exist, it wil not be included in the scanned directories");
			} else if (!sourceDir.canRead()) {
				getLog().warn("The source directory: " + sourceDir.getAbsolutePath() + " can not be read, it wil not be included in the scanned directories");
			} else if (!sourceDir.isDirectory()) {
				getLog()
						.warn("The source directory: " + sourceDir.getAbsolutePath() + " is not a directory, it wil not be included in the scanned directories");
			} else {
				processSourceDirectory(sourceDir, merge.getIncludes());
			}
		}
	}

	/**
	 * This builds up the list of ordering names to use
	 * @param merge the merge config to build the orderings for
	 */
	private void buildOrderings(Merge merge) {
		this.orderingNames = new ArrayList<String>();
		if (merge.getNameContainsOrderings() != null && merge.getNameContainsOrderings().length > 0) {
			for (String ordering : merge.getNameContainsOrderings()) {
				if (ordering != null && ordering.trim().length() > 0) {
					getLog().debug("Adding ordering name: " + ordering.trim());
					this.orderingNames.add(ordering.trim());
				} else {
					getLog().warn("The ordering name: " + ordering + " is empty it will be ignored.");
				}
			}
			this.orderingNames.add(DEFAULT_ORDERING_NAME);
		}

		this.useOrdering = true;
		if (this.orderingNames.isEmpty()) {
			this.orderingNames = DEFAULT_ORDERING;
			this.useOrdering = false;
		}
	}

	boolean fileMatches(String fileName, String[] includes) {
		if (includes != null && includes.length > 0) {
			for (String include : includes) {
				if (fileName.contains(include)) {
					return true;
				}
			}
			return false;
		}
		return true;
	}

	private void processSourceDirectory(File sourceDir, final String[] includes) {
		getLog().info("Scanning sourced directory: " + sourceDir.getAbsolutePath() + " for files to merge...");
		// first find matching files
		File[] matchingFiles = sourceDir.listFiles(new FileFilter() {

			/**
			 * {@inheritDoc}
			 * @see java.io.FileFilter#accept(java.io.File)
			 */
			public boolean accept(File pathname) {
				// find matching files
				if (pathname.isFile()) {
					if (fileMatches(pathname.getName(), includes)) {
						if (pathname.canRead()) {
							return true;
						} else {
							getLog().warn("The file: " + pathname.getAbsolutePath() + " can not be read, it will not be included in the files to be appended.");
							return false;
						}
					} else {
						getLog().debug(
								"The file: " + pathname.getAbsolutePath()
										+ " does not match the defined file includes, it will not be included in the files to be appended.");
					}

				}
				return false;
			}

		});

		int numFiles = matchingFiles == null ? 0 : matchingFiles.length;
		getLog().info("Sourced directory: " + sourceDir.getAbsolutePath() + " contains " + numFiles + " files to merge.");

		// add the sql files to the appropriate list for ordering
		if (matchingFiles != null) {
			for (File file : matchingFiles) {

				List<File> targetList = this.orderedFiles.get(DEFAULT_ORDERING_NAME);
				// find the list to add the file to, if no list matches it gets added to the default list
				for (String orderingName : this.orderingNames) {
					if (this.useOrdering) {
						if (file.getName().contains(orderingName)) {
							getLog().debug("Adding file: " + file.getAbsolutePath() + " to the list of files matching: " + orderingName);
							targetList = this.orderedFiles.get(orderingName);
							if (targetList == null) {
								targetList = new ArrayList<File>();
								this.orderedFiles.put(orderingName, targetList);
							}
							break;
						}
					}
				}

				// if target list still null use default
				if (targetList == null) {
					targetList = new ArrayList<File>();
					this.orderedFiles.put(DEFAULT_ORDERING_NAME, targetList);
				}

				targetList.add(file);
			}
		}

		// now find sub directories
		File[] subDirs = sourceDir.listFiles(new FileFilter() {

			/**
			 * {@inheritDoc}
			 * @see java.io.FileFilter#accept(java.io.File)
			 */
			public boolean accept(File pathname) {
				// return dirs
				if (pathname.isDirectory()) {
					if (pathname.canRead()) {
						return true;
					} else {
						getLog().warn(
								"The directory: " + pathname.getAbsolutePath() + " can not be read, it will not be included in the directories to be scanned.");
						return false;
					}
				}
				return false;
			}

		});

		numFiles = subDirs == null ? 0 : subDirs.length;
		getLog().debug("Sourced directory: " + sourceDir.getAbsolutePath() + " contains " + numFiles + " sub directories to scan.");

		// recursively process sub directories
		if (subDirs != null) {
			for (File subDir : subDirs) {
				processSourceDirectory(subDir, includes);
			}
		}
	}

}
