/*
 * Copyright © 2012 Avego Ltd., All Rights Reserved.
 * For licensing terms please contact Avego LTD.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package croche.maven.plugin.version;

/**
 * The InvalidVersionException represents an invalid version
 * @version $Id$
 * @author conorroche
 */
public class InvalidVersionException extends Exception {

	/**
	 * This
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * This creates a InvalidVersionException
	 * @param message
	 */
	public InvalidVersionException(String message) {
		super(message);
	}

}
