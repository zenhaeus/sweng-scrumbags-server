package ch.epfl.scrumtool.server.exception;

import com.google.api.server.spi.ServiceException;
/**
 * 
 * @author aschneuw
 *
 */
public class EntityExistsException extends ServiceException {
    private static final int ERROR_CODE = 408;
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public EntityExistsException(String message) {
        super(ERROR_CODE, message);
    }
}