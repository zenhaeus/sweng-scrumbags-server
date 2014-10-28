package ch.epfl.scrumtool.server;

import ch.epfl.scrumtool.PMF;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.datanucleus.query.JDOCursorHelper;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

@Api(
        name = "scrumtool",
        version = "v1",
        namespace = @ApiNamespace(ownerDomain = "epfl.ch", ownerName = "epfl.ch", packagePath = "scrumtool.server"),
        clientIds = {Constants.ANDROID_CLIENT_ID},
        audiences = {Constants.ANDROID_AUDIENCE}
        )
public class SprintEndpoint {

	/**
	 * This method lists all the entities inserted in datastore.
	 * It uses HTTP GET method and paging support.
	 *
	 * @return A CollectionResponse class containing the list of all entities
	 * persisted and a cursor to the next page.
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@ApiMethod(name = "listSprint")
	public CollectionResponse<Sprint> listSprint(
			@Nullable @Named("cursor") String cursorString,
			@Nullable @Named("limit") Integer limit) {

		PersistenceManager mgr = null;
		Cursor cursor = null;
		List<Sprint> execute = null;

		try {
			mgr = getPersistenceManager();
			Query query = mgr.newQuery(Sprint.class);
			if (cursorString != null && cursorString != "") {
				cursor = Cursor.fromWebSafeString(cursorString);
				HashMap<String, Object> extensionMap = new HashMap<String, Object>();
				extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
				query.setExtensions(extensionMap);
			}

			if (limit != null) {
				query.setRange(0, limit);
			}

			execute = (List<Sprint>) query.execute();
			cursor = JDOCursorHelper.getCursor(execute);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();

			// Tight loop for fetching all entities from datastore and accomodate
			// for lazy fetch.
			for (Sprint obj : execute)
				;
		} finally {
			mgr.close();
		}

		return CollectionResponse.<Sprint> builder().setItems(execute)
				.setNextPageToken(cursorString).build();
	}

	/**
	 * This method gets the entity having primary key id. It uses HTTP GET method.
	 *
	 * @param id the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getSprint")
	public Sprint getSprint(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		Sprint sprint = null;
		try {
			sprint = mgr.getObjectById(Sprint.class, id);
		} finally {
			mgr.close();
		}
		return sprint;
	}

	/**
	 * This inserts a new entity into App Engine datastore. If the entity already
	 * exists in the datastore, an exception is thrown.
	 * It uses HTTP POST method.
	 *
	 * @param sprint the entity to be inserted.
	 * @return The inserted entity.
	 */
	@ApiMethod(name = "insertSprint")
	public Sprint insertSprint(Sprint sprint) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (containsSprint(sprint)) {
				throw new EntityExistsException("Object already exists");
			}
			mgr.makePersistent(sprint);
		} finally {
			mgr.close();
		}
		return sprint;
	}

	/**
	 * This method is used for updating an existing entity. If the entity does not
	 * exist in the datastore, an exception is thrown.
	 * It uses HTTP PUT method.
	 *
	 * @param sprint the entity to be updated.
	 * @return The updated entity.
	 */
	@ApiMethod(name = "updateSprint")
	public Sprint updateSprint(Sprint sprint) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (!containsSprint(sprint)) {
				throw new EntityNotFoundException("Object does not exist");
			}
			mgr.makePersistent(sprint);
		} finally {
			mgr.close();
		}
		return sprint;
	}

	/**
	 * This method removes the entity with primary key id.
	 * It uses HTTP DELETE method.
	 *
	 * @param id the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "removeSprint")
	public void removeSprint(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			Sprint sprint = mgr.getObjectById(Sprint.class, id);
			mgr.deletePersistent(sprint);
		} finally {
			mgr.close();
		}
	}

	private boolean containsSprint(Sprint sprint) {
		PersistenceManager mgr = getPersistenceManager();
		boolean contains = true;
		try {
			mgr.getObjectById(Sprint.class, sprint.getKey());
		} catch (javax.jdo.JDOObjectNotFoundException ex) {
			contains = false;
		} finally {
			mgr.close();
		}
		return contains;
	}

	private static PersistenceManager getPersistenceManager() {
		return PMF.get().getPersistenceManager();
	}

}
