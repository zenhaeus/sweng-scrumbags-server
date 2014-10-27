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

@Api(name = "maintaskendpoint", namespace = @ApiNamespace(ownerDomain = "epfl.ch", ownerName = "epfl.ch", packagePath = "entity"))
public class MainTaskEndpoint {

	/**
	 * This method lists all the entities inserted in datastore.
	 * It uses HTTP GET method and paging support.
	 *
	 * @return A CollectionResponse class containing the list of all entities
	 * persisted and a cursor to the next page.
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@ApiMethod(name = "listMainTask")
	public CollectionResponse<MainTask> listMainTask(
			@Nullable @Named("cursor") String cursorString,
			@Nullable @Named("limit") Integer limit) {

		PersistenceManager mgr = null;
		Cursor cursor = null;
		List<MainTask> execute = null;

		try {
			mgr = getPersistenceManager();
			Query query = mgr.newQuery(MainTask.class);
			if (cursorString != null && cursorString != "") {
				cursor = Cursor.fromWebSafeString(cursorString);
				HashMap<String, Object> extensionMap = new HashMap<String, Object>();
				extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
				query.setExtensions(extensionMap);
			}

			if (limit != null) {
				query.setRange(0, limit);
			}

			execute = (List<MainTask>) query.execute();
			cursor = JDOCursorHelper.getCursor(execute);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();

			// Tight loop for fetching all entities from datastore and accomodate
			// for lazy fetch.
			for (MainTask obj : execute)
				;
		} finally {
			mgr.close();
		}

		return CollectionResponse.<MainTask> builder().setItems(execute)
				.setNextPageToken(cursorString).build();
	}

	/**
	 * This method gets the entity having primary key id. It uses HTTP GET method.
	 *
	 * @param id the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getMainTask")
	public MainTask getMainTask(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		MainTask maintask = null;
		try {
			maintask = mgr.getObjectById(MainTask.class, id);
		} finally {
			mgr.close();
		}
		return maintask;
	}

	/**
	 * This inserts a new entity into App Engine datastore. If the entity already
	 * exists in the datastore, an exception is thrown.
	 * It uses HTTP POST method.
	 *
	 * @param maintask the entity to be inserted.
	 * @return The inserted entity.
	 */
	@ApiMethod(name = "insertMainTask")
	public MainTask insertMainTask(MainTask maintask) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (containsMainTask(maintask)) {
				throw new EntityExistsException("Object already exists");
			}
			mgr.makePersistent(maintask);
		} finally {
			mgr.close();
		}
		return maintask;
	}

	/**
	 * This method is used for updating an existing entity. If the entity does not
	 * exist in the datastore, an exception is thrown.
	 * It uses HTTP PUT method.
	 *
	 * @param maintask the entity to be updated.
	 * @return The updated entity.
	 */
	@ApiMethod(name = "updateMainTask")
	public MainTask updateMainTask(MainTask maintask) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (!containsMainTask(maintask)) {
				throw new EntityNotFoundException("Object does not exist");
			}
			mgr.makePersistent(maintask);
		} finally {
			mgr.close();
		}
		return maintask;
	}

	/**
	 * This method removes the entity with primary key id.
	 * It uses HTTP DELETE method.
	 *
	 * @param id the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "removeMainTask")
	public void removeMainTask(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			MainTask maintask = mgr.getObjectById(MainTask.class, id);
			mgr.deletePersistent(maintask);
		} finally {
			mgr.close();
		}
	}

	private boolean containsMainTask(MainTask maintask) {
		PersistenceManager mgr = getPersistenceManager();
		boolean contains = true;
		try {
			mgr.getObjectById(MainTask.class, maintask.getKey());
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
