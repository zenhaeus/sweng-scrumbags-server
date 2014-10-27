package ch.epfl.entity;

import ch.epfl.entity.PMF;

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

@Api(name = "priorityendpoint", namespace = @ApiNamespace(ownerDomain = "epfl.ch", ownerName = "epfl.ch", packagePath = "entity"))
public class PriorityEndpoint {

	/**
	 * This method lists all the entities inserted in datastore.
	 * It uses HTTP GET method and paging support.
	 *
	 * @return A CollectionResponse class containing the list of all entities
	 * persisted and a cursor to the next page.
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@ApiMethod(name = "listPriority")
	public CollectionResponse<Priority> listPriority(
			@Nullable @Named("cursor") String cursorString,
			@Nullable @Named("limit") Integer limit) {

		PersistenceManager mgr = null;
		Cursor cursor = null;
		List<Priority> execute = null;

		try {
			mgr = getPersistenceManager();
			Query query = mgr.newQuery(Priority.class);
			if (cursorString != null && cursorString != "") {
				cursor = Cursor.fromWebSafeString(cursorString);
				HashMap<String, Object> extensionMap = new HashMap<String, Object>();
				extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
				query.setExtensions(extensionMap);
			}

			if (limit != null) {
				query.setRange(0, limit);
			}

			execute = (List<Priority>) query.execute();
			cursor = JDOCursorHelper.getCursor(execute);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();

			// Tight loop for fetching all entities from datastore and accomodate
			// for lazy fetch.
			for (Priority obj : execute)
				;
		} finally {
			mgr.close();
		}

		return CollectionResponse.<Priority> builder().setItems(execute)
				.setNextPageToken(cursorString).build();
	}

	/**
	 * This method gets the entity having primary key id. It uses HTTP GET method.
	 *
	 * @param id the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getPriority")
	public Priority getPriority(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		Priority priority = null;
		try {
			priority = mgr.getObjectById(Priority.class, id);
		} finally {
			mgr.close();
		}
		return priority;
	}

	/**
	 * This inserts a new entity into App Engine datastore. If the entity already
	 * exists in the datastore, an exception is thrown.
	 * It uses HTTP POST method.
	 *
	 * @param priority the entity to be inserted.
	 * @return The inserted entity.
	 */
	@ApiMethod(name = "insertPriority")
	public Priority insertPriority(Priority priority) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (containsPriority(priority)) {
				throw new EntityExistsException("Object already exists");
			}
			mgr.makePersistent(priority);
		} finally {
			mgr.close();
		}
		return priority;
	}

	/**
	 * This method is used for updating an existing entity. If the entity does not
	 * exist in the datastore, an exception is thrown.
	 * It uses HTTP PUT method.
	 *
	 * @param priority the entity to be updated.
	 * @return The updated entity.
	 */
	@ApiMethod(name = "updatePriority")
	public Priority updatePriority(Priority priority) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (!containsPriority(priority)) {
				throw new EntityNotFoundException("Object does not exist");
			}
			mgr.makePersistent(priority);
		} finally {
			mgr.close();
		}
		return priority;
	}

	/**
	 * This method removes the entity with primary key id.
	 * It uses HTTP DELETE method.
	 *
	 * @param id the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "removePriority")
	public void removePriority(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			Priority priority = mgr.getObjectById(Priority.class, id);
			mgr.deletePersistent(priority);
		} finally {
			mgr.close();
		}
	}

	private boolean containsPriority(Priority priority) {
		PersistenceManager mgr = getPersistenceManager();
		boolean contains = true;
		try {
			mgr.getObjectById(Priority.class, priority.getKey());
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
