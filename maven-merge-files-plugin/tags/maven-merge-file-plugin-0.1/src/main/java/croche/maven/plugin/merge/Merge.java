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

import java.io.File;
import java.util.Arrays;
   
/**
 * The Merge represents a POJO to hold the merge configuration for a set of files
 * @version $Id$
 * @author conorroche
 */
public class Merge {

	private File targetFile;
	private File[] sourceDirs;
	private String[] nameContainsOrderings;
	private String[] includes;
	private String separator;
	private String encoding;

	/**
	 * The target file that the files in this merge will be merged into
	 * @parameter
	 * @required
	 * @return the targetFile
	 */
	public File getTargetFile() {
		return this.targetFile;
	}

	/**
	 * The source directories where it will scan for files, it will look in these directories and all of their subdirectories
	 * @parameter
	 * @required
	 * @return the sourceDirs The source directories to scan for files in
	 */
	public File[] getSourceDirs() {
		return this.sourceDirs;
	}

	/**
	 * This is an optional set of strings that file names can contain that control the ordering of files merged into the final output file.
	 * For example if this contained create-schema,schema-objects,indices,data this would mean that first files whose name contained create-schema would be
	 * appended,
	 * then files whose name contained schema-objects, then files whose names contained indices and finally files whose names contained data would get appended.
	 * @parameter
	 */
	public String[] getNameContainsOrderings() {
		return this.nameContainsOrderings;
	}

	/**
	 * This is an optional set of file includes to look for, matching files name must contain one of these values,
	 * if not specified then all files found will be merged into the target file
	 * @parameter
	 * @return The substrings that matching files name must contain
	 */
	public String[] getIncludes() {
		return this.includes;
	}

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
	 * @return the separator to use above files
	 */
	public String getSeparator() {
		return this.separator;
	}

	/**
	 * This is an optional encoding to use when reading/writing the files being merged, if not specified
	 * then UTF-8 will be used
	 * @parameter
	 * @return the encoding to use for reading/writing files
	 */
	public String getEncoding() {
		return this.encoding;
	}

	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Merge [encoding=").append(this.encoding).append(", includes=").append(Arrays.toString(this.includes))
				.append(", nameContainsOrderings=").append(Arrays.toString(this.nameContainsOrderings)).append(", separator=").append(this.separator).append(
						", sourceDirs=").append(Arrays.toString(this.sourceDirs)).append(", targetFile=").append(this.targetFile).append("]");
		return builder.toString();
	}

}
