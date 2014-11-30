package ch.epfl.scrumtool.server;

import java.io.Serializable;

/**
 * Used to return the result of a DS operation to the client
 * 
 * @author aschneuw
 * 
 */
public class KeyResponse implements Serializable {
    private static final long serialVersionUID = 5872067343145587448L;
    private final String key;

    
    public KeyResponse(String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        
        if (key.equals("")) {
            throw new IllegalArgumentException("Key can't be an empty String");
            
        }
        this.key = key;
    }
    
    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }
}
