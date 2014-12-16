package ch.epfl.scrumtool.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;

import org.junit.Test;

/**
 * @author zenhaeus
 *
 */
public class ScrumUserTest {
    private static final String USER_KEY = "some@example.com";
    private static final String COMPANY_NAME = "Company";
    private static final long DATE_OF_BIRTH = Calendar.getInstance().getTimeInMillis();
    private static final String JOB_TITLE = "CEO";
    private static final String NAME = "Name";
    private static final String LASTNAME = "Lastname";
    private static final long LAST_MOD_DATE = Calendar.getInstance().getTimeInMillis();
    private static final String LAST_MOD_USER = USER_KEY;
    private static final String GENDER = "male";

    private static ScrumUser user = new ScrumUser();

    @Test
    public void testSetGetEmail() {
        user.setEmail(USER_KEY);
        assertEquals(USER_KEY, user.getEmail());
    }

    @Test
    public void testSetGetCompanyName() {
        user.setCompanyName(COMPANY_NAME);
        assertEquals(COMPANY_NAME, user.getCompanyName());
    }

    @Test
    public void testSetGetDateOfBirth() {
        user.setDateOfBirth(DATE_OF_BIRTH);
        assertEquals(DATE_OF_BIRTH, user.getDateOfBirth());
    }
    
    @Test
    public void testSetGetJobTitle() {
        user.setJobTitle(JOB_TITLE);
        assertEquals(JOB_TITLE, user.getJobTitle());
    }
    
    @Test
    public void testSetGetName() {
        user.setName(NAME);
        assertEquals(NAME, user.getName());
    }
    
    @Test
    public void testSetGetLastName() {
        user.setLastName(LASTNAME);
        assertEquals(LASTNAME, user.getLastName());
    }
    
    @Test
    public void testSetGetLastModDate() {
        user.setLastModDate(LAST_MOD_DATE);
        assertEquals(LAST_MOD_DATE, user.getLastModDate());
    }
    
    @Test
    public void testSetGetLastModUser() {
        user.setLastModUser(USER_KEY);
        assertEquals(LAST_MOD_USER, user.getLastModUser());
    }
    
    @Test
    public void testSetGetGender() {
        user.setGender(GENDER);
        assertEquals(GENDER, user.getGender());
    }
    
    @Test
    public void testAddPlayer() {
        ScrumPlayer newPlayer = new ScrumPlayer();
        user.addPlayer(newPlayer);
        assertTrue(user.getPlayers().size() == 1);
        assertTrue(user.getPlayers().contains(newPlayer));
    }
    
    @Test
    public void testRemovePlayer() {
        ScrumPlayer newPlayer = new ScrumPlayer();
        user.addPlayer(newPlayer);
        user.removePlayer(newPlayer);
        assertTrue(!user.getPlayers().contains(newPlayer));
    }
}
