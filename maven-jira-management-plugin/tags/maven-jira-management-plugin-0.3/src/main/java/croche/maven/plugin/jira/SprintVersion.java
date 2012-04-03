/*
 * Copyright © 2011 Avego Ltd., All Rights Reserved.
 * For licensing terms please contact Avego LTD.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package croche.maven.plugin.jira;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * The SprintVersion represents a sprint version and models the Year, Quarter and quarter section
 * FIXME how to handle 5 wk sprints?
 * @version $Id$
 * @author conorroche
 */
public class SprintVersion {

	private int patch = 0;
	private int quarter;
	private int section;
	private int year;

	/**
	 * This creates a DbVersion
	 */
	public SprintVersion() {
		super();
	}

	/**
	 * This creates a DbVersion
	 * @param year This is the year of the version
	 * @param quarter This is the quarter e.g. Q1,Q2,Q3 or Q4
	 * @param section This is the section of the quarter e.g. Q3.2 means the second section
	 *            of the third quarter
	 * @param patch The patch version, 0 indicates no patch version
	 */
	public SprintVersion(int year, int quarter, int section, int patch) {
		super();
		this.year = year;
		this.quarter = quarter;
		this.section = section;
		this.patch = patch;
	}

	/**
	 * This creates a DbVersion by parsing the given version string,
	 * The version should be 9 or 11 characters long in the form YYYY-Q(1-4).(1-4).N for example 2011-Q3.1.0
	 * @param version The version should be 11or 12 characters long in the form YYYY-Q(1-4).(1-4).N for example 2011-Q3.1.0
	 * @throws IllegalArgumentException If the db version could not be parsed
	 */
	public SprintVersion(String version) throws IllegalArgumentException {
		if (version == null) {
			throw new IllegalArgumentException("The version was null");
		}
		if (version.length() == 9 || version.length() == 11) {
			int year = 0;
			try {
				year = Integer.parseInt(version.substring(0, 4));
			} catch (NumberFormatException nfe) {
				throw new IllegalArgumentException("The year could not be parsed from the version: " + version, nfe);
			}
			Calendar now = Calendar.getInstance();
			int currentYear = now.get(Calendar.YEAR);
			if (year >= 2011 && year <= currentYear) {
				this.year = year;
				this.quarter = parseOneToFour(version, version.charAt(6));
				this.section = parseOneToFour(version, version.charAt(8));
				if (version.length() == 11) {
					this.patch = Integer.parseInt(version.substring(10));
				}
			} else {
				throw new IllegalArgumentException("The year must be between 2011 and the current year: " + currentYear + " but was: " + year);
			}
		} else {
			throw new IllegalArgumentException("The version should be 9 or 11 characters long in the form "
					+ "YYYY-Q(1-4).(1-4).N or YYYY-Q(1-4).(1-4) for example 2011-Q3.1.1 or 2011-Q3.1 but was: " + version);
		}
	}

	/**
	 * This gets the quarter
	 * @return the quarter
	 */
	public int getQuarter() {
		return this.quarter;
	}

	/**
	 * This gets the section
	 * @return the section
	 */
	public int getSection() {
		return this.section;
	}

	/**
	 * This gets the versions between this version and the given version
	 * @param toVersion The version to get versions up to
	 * @return A list of versions between this one and the given one
	 * @throws IllegalArgumentException if the to version is before this one
	 */
	public List<SprintVersion> getVersionsTo(SprintVersion toVersion) throws IllegalArgumentException {
		List<SprintVersion> versions = new ArrayList<SprintVersion>();

		if (toVersion.getYear() < getYear()) {
			throw new IllegalArgumentException("The to version: " + toVersion + " can not be before this: " + this);
		} else if (toVersion.getYear() == getYear()) {
			if (toVersion.getQuarter() < getQuarter()) {
				throw new IllegalArgumentException("The to version: " + toVersion + " can not be before this: " + this);
			} else if (toVersion.getQuarter() == getQuarter()) {
				if (toVersion.getSection() < getSection()) {
					throw new IllegalArgumentException("The to version: " + toVersion + " can not be before this: " + this);
				} else if (toVersion.getSection() == getSection()) {
					if (toVersion.getPatch() <= getPatch()) {
						throw new IllegalArgumentException("The to version: " + toVersion + " can not be before this: " + this);
					}
				}
			}
		}

		SprintVersion nextVersion = this;
		while (true) {

			if (toVersion.getPatch() > 0 && toVersion.isSameBranch(nextVersion)) {
				nextVersion = nextVersion.nextVersion(true);
			} else {
				nextVersion = nextVersion.nextVersion(false);
			}

			versions.add(nextVersion);
			if (nextVersion.equals(toVersion)) {
				break;
			}
		}

		return versions;
	}

	/**
	 * This gets the year
	 * @return the year
	 */
	public int getYear() {
		return this.year;
	}

	/**
	 * This gets the patch
	 * @return the patch
	 */
	public int getPatch() {
		return this.patch;
	}

	/**
	 * This sets the patch
	 * @param patch the patch to set
	 */
	public void setPatch(int patch) {
		this.patch = patch;
	}

	/**
	 * This gets the next version after this version
	 * @param incrementPatch if true it means that the next version after for example
	 *            Q2.3.0 would be Q2.3.1 otherwise it would be Q2.4.0
	 * @return The next version
	 */
	public SprintVersion nextVersion(boolean incrementPatch) {
		SprintVersion next = new SprintVersion();
		// see if we need to increment the year
		if (!incrementPatch && getQuarter() == 4 && getSection() == 4) {
			next.setYear(getYear() + 1);
			next.setQuarter(1);
			next.setSection(1);
		} else if (!incrementPatch && getSection() == 4) {
			// increment the quarter
			next.setYear(getYear());
			next.setQuarter(getQuarter() + 1);
			next.setSection(1);
		} else {
			// just increment the section
			next.setYear(getYear());
			next.setQuarter(getQuarter());
			if (incrementPatch) {
				next.setSection(getSection());
				next.setPatch(getPatch() + 1);
			} else {
				next.setSection(getSection() + 1);
			}
		}
		return next;
	}

	/**
	 * This gets whether the given version is the same branch as this one, this
	 * means that its the same apart from the patch version
	 * @param version The version to check
	 * @return True if the given version is the same as this one bar the patch
	 */
	public boolean isSameBranch(SprintVersion version) {
		if (version != null) {
			if (version.getYear() != this.getYear()) {
				return false;
			} else if (version.getQuarter() != this.getQuarter()) {
				return false;
			} else if (version.getSection() != this.getSection()) {
				return false;
			} else {
				return true;
			}
		}
		return false;
	}

	private int parseOneToFour(String version, char c) throws IllegalArgumentException {
		if (c == '1') {
			return 1;
		} else if (c == '2') {
			return 2;
		} else if (c == '3') {
			return 3;
		} else if (c == '4') {
			return 4;
		}
		throw new IllegalArgumentException("The quarter could not be parsed from the version: " + version + " as the character for it: " + c + " was not 1-4");
	}

	/**
	 * This sets the quarter
	 * @param quarter the quarter to set
	 */
	public void setQuarter(int quarter) {
		this.quarter = quarter;
	}

	/**
	 * This sets the section
	 * @param section the section to set
	 */
	public void setSection(int section) {
		this.section = section;
	}

	/**
	 * This sets the year
	 * @param year the year to set
	 */
	public void setYear(int year) {
		this.year = year;
	}

	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.patch;
		result = prime * result + this.quarter;
		result = prime * result + this.section;
		result = prime * result + this.year;
		return result;
	}

	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SprintVersion other = (SprintVersion) obj;
		if (this.patch != other.patch) {
			return false;
		}
		if (this.quarter != other.quarter) {
			return false;
		}
		if (this.section != other.section) {
			return false;
		}
		if (this.year != other.year) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.year).append("-Q").append(this.quarter).append('.').append(this.section).append('.').append(this.patch);
		return builder.toString();
	}

}
