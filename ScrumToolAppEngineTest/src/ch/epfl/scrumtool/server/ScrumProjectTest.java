package ch.epfl.scrumtool.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.HashSet;

import org.junit.Test;

public class ScrumProjectTest {
    private final static String KEY = "key";
    private final static String NAME = "Murcs";
    private final static String DESCRIPTION = "description";
    private static final long DATE = Calendar.getInstance().getTimeInMillis();
    private static final String LAST_USER = "example@mock.ch";

    private static ScrumProject project = new ScrumProject();

    @Test
    public void testSetGetKey() {
        project.setKey(KEY);
        assertEquals(KEY, project.getKey());
    }

    @Test
    public void testSetGetName() {
        project.setName(NAME);
        assertEquals(NAME, project.getName());
    }

    @Test
    public void testSetGetDescription() {
        project.setDescription(DESCRIPTION);
        assertEquals(DESCRIPTION, project.getDescription());
    }

    @Test
    public void testSetGetLastModDate() {
        project.setLastModDate(DATE);
        assertEquals(DATE, project.getLastModDate());
    }

    @Test
    public void testSetGetLastModUser() {
        project.setLastModUser(LAST_USER);
        assertEquals(LAST_USER, project.getLastModUser());
    }
    
    @Test
    public void testSetGetBacklog() {
        HashSet<ScrumMainTask> backlog = new HashSet<ScrumMainTask>();
        project.setBacklog(backlog);
        assertEquals(backlog, project.getBacklog());
    }
    
    @Test
    public void testAddRemoveMaintask() {
        ScrumMainTask maintask = new ScrumMainTask();
        project.addMaintask(maintask);
        assertEquals(project.getBacklog().size(),1);
        assertTrue(project.getBacklog().contains(maintask));
        project.removeMaintask(maintask);
        assertEquals(project.getBacklog().size(), 0);
        assertFalse(project.getBacklog().contains(maintask));
    }
    
    @Test
    public void testSetGetPlayers() {
        HashSet<ScrumPlayer> players = new HashSet<ScrumPlayer>();
        project.setPlayers(players);
        assertEquals(players, project.getPlayers());
    }
    
    @Test
    public void testAddRemovePlayer() {
        ScrumPlayer player = new ScrumPlayer();
        project.addPlayer(player);
        assertEquals(project.getPlayers().size(),1);
        assertTrue(project.getPlayers().contains(player));
        project.removePlayer(player);
        assertEquals(project.getPlayers().size(), 0);
        assertFalse(project.getPlayers().contains(player));
    }
    
    @Test
    public void testSetGetSprints() {
        HashSet<ScrumSprint> sprints = new HashSet<ScrumSprint>();
        project.setSprints(sprints);
        assertEquals(sprints, project.getSprints());
    }
    
    @Test
    public void testAddRemoveSprint() {
        ScrumSprint sprint = new ScrumSprint();
        project.addSprint(sprint);
        assertEquals(project.getSprints().size(),1);
        assertTrue(project.getSprints().contains(sprint));
        project.removeSprint(sprint);
        assertEquals(project.getSprints().size(), 0);
        assertFalse(project.getSprints().contains(sprint));
    }
}
