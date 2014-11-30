package ch.epfl.scrumtool;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.response.InternalServerErrorException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.users.User;

/**
 * @author aschneuw
 * 
 */
public class AppEngineUtils {

    /**
     * @param user
     * @throws UnauthorizedException
     */
    public static void basicAuthentication(User user)
            throws UnauthorizedException {
        if (user == null) {
            throw new UnauthorizedException("Unauthenticated request no allowed");
        }
    }

    public static <A> A getObjectFromDatastore(Class<A> type, String key, PersistenceManager pm)
            throws ServiceException {
        if (type == null || key == null || pm == null) {
            throw new InternalServerErrorException(
                    new IllegalArgumentException());
        }

        try {
            return pm.getObjectById(type, key);

        } catch (JDOObjectNotFoundException e) {
            throw new NotFoundException(e);
        }
    }
}
