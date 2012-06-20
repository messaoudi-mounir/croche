/*
 * Copyright © 2011 Avego Ltd., All Rights Reserved.
 * For licensing terms please contact Avego LTD.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package croche.maven.plugin.dbupgrade;

/**
 * The SprintVersionException represents an exception with a sprint version
 * @version $Id$
 * @author conorroche
 */
public class SprintVersionException extends Exception {

	/**
	 * This
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * This creates a SprintVersionException
	 */
	public SprintVersionException() {
	}

	/**
	 * This creates a SprintVersionException
	 * @param message
	 */
	public SprintVersionException(String message) {
		super(message);
	}

	/**
	 * This creates a SprintVersionException
	 * @param cause
	 */
	public SprintVersionException(Throwable cause) {
		super(cause);
	}

	/**
	 * This creates a SprintVersionException
	 * @param message
	 * @param cause
	 */
	public SprintVersionException(String message, Throwable cause) {
		super(message, cause);
	}

}
