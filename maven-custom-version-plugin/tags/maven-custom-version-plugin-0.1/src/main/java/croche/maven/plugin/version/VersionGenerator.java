/*
 * Copyright 2011 Conor Roche
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
package croche.maven.plugin.version;

/**
 * The VersionGenerator represents a generator that can work out what the release version should be and what
 * the next development version should be given the version configuration for the plugin
 * @version $Id$
 * @author conorroche
 */
public interface VersionGenerator {
	
	/**
	 * This generates the release version from the given
	 * configuration, if the config does not require a custom version to be generated it will return null.
	 * @param config The version configuration
	 * @param currentVersion The current version of the artifact being released
	 * @return The release version or null if the config does not require a custom release version to be generated.
	 */
	public String generateReleaseVersion(VersionConfig config, String currentVersion);
	
	/**
	 * This generates the development version e.g. the next version post release from the given
	 * configuration, if the config does not require a custom version to be generated it will return null.
	 * @param config The version configuration
	 * @param currentVersion The current version of the artifact being released
	 * @return The development version or null if the config does not require a custom development version to be generated.
	 */
	public String generateDevelopmentVersion(VersionConfig config, String currentVersion);

}
