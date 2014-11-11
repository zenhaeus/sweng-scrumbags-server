/**
 * 
 */
package ch.epfl.scrumtool;

import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;

/**
 * @author Arno
 * 
 */
public class AppEngineUtils {

    /**
     * @param user
     * @throws OAuthRequestException
     */
    public static void basicAuthentication(User user)
            throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("Invalid login");
        }
    }
}
