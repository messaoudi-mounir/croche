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

	/**
	 * The target file that the files in this merge will be merged into
	 * @parameter
	 * @required
	 */
	private File targetFile;

	/**
	 * The source directories where it will scan for files, it will look in these directories and all of their subdirectories
	 * @parameter
	 * @required
	 */
	private File[] sourceDirs;

	/**
	 * This is an optional set of strings that file names can contain that control the ordering of files merged into the final output file.
	 * For example if this contained create-schema,schema-objects,indices,data this would mean that first files whose name contained create-schema would be
	 * appended,
	 * then files whose name contained schema-objects, then files whose names contained indices and finally files whose names contained data would get appended.
	 * @parameter
	 */
	private String[] nameContainsOrderings;

	/**
	 * This is an optional set of file extensions to look for, matching files must end with one of these values
	 * @parameter
	 */
	private String[] extensions;

	/**
	 * This is an optional separator output between merged files in the combined file
	 * @parameter
	 */
	private String separator;

	/**
	 * This is an optional encoding to use when reading/writing the files being merged, if not specified
	 * then UTF-8 will be used
	 * @parameter
	 */
	private String encoding;

	/**
	 * This gets the targetFile
	 * @return the targetFile
	 */
	public File getTargetFile() {
		return this.targetFile;
	}

	/**
	 * This gets the sourceDirs
	 * @return the sourceDirs
	 */
	public File[] getSourceDirs() {
		return this.sourceDirs;
	}

	/**
	 * This gets the nameContainsOrderings
	 * @return the nameContainsOrderings
	 */
	public String[] getNameContainsOrderings() {
		return this.nameContainsOrderings;
	}

	/**
	 * This gets the extensions
	 * @return the extensions
	 */
	public String[] getExtensions() {
		return this.extensions;
	}

	/**
	 * This gets the separator
	 * @return the separator
	 */
	public String getSeparator() {
		return this.separator;
	}

	/**
	 * This gets the encoding
	 * @return the encoding
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
		builder.append("Merge [encoding=").append(this.encoding).append(", extensions=").append(Arrays.toString(this.extensions)).append(
				", nameContainsOrderings=").append(Arrays.toString(this.nameContainsOrderings)).append(", separator=").append(this.separator).append(
				", sourceDirs=").append(Arrays.toString(this.sourceDirs)).append(", targetFile=").append(this.targetFile).append("]");
		return builder.toString();
	}

}
