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
public class ScrumUserEndpoint {

	/**
	 * This method lists all the entities inserted in datastore.
	 * It uses HTTP GET method and paging support.
	 *
	 * @return A CollectionResponse class containing the list of all entities
	 * persisted and a cursor to the next page.
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@ApiMethod(name = "listScrumUser")
	public CollectionResponse<ScrumUser> listScrumUser(
			@Nullable @Named("cursor") String cursorString,
			@Nullable @Named("limit") Integer limit) {

		PersistenceManager mgr = null;
		Cursor cursor = null;
		List<ScrumUser> execute = null;

		try {
			mgr = getPersistenceManager();
			Query query = mgr.newQuery(ScrumUser.class);
			if (cursorString != null && cursorString != "") {
				cursor = Cursor.fromWebSafeString(cursorString);
				HashMap<String, Object> extensionMap = new HashMap<String, Object>();
				extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
				query.setExtensions(extensionMap);
			}

			if (limit != null) {
				query.setRange(0, limit);
			}

			execute = (List<ScrumUser>) query.execute();
			cursor = JDOCursorHelper.getCursor(execute);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();

			// Tight loop for fetching all entities from datastore and accomodate
			// for lazy fetch.
			for (ScrumUser obj : execute)
				;
		} finally {
			mgr.close();
		}

		return CollectionResponse.<ScrumUser> builder().setItems(execute)
				.setNextPageToken(cursorString).build();
	}

	/**
	 * This method gets the entity having primary key id. It uses HTTP GET method.
	 *
	 * @param id the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getScrumUser")
	public ScrumUser getScrumUser(@Named("id") String id) {
		PersistenceManager mgr = getPersistenceManager();
		ScrumUser scrumuser = null;
		try {
			scrumuser = mgr.getObjectById(ScrumUser.class, id);
		} finally {
			mgr.close();
		}
		return scrumuser;
	}

	/**
	 * This inserts a new entity into App Engine datastore. If the entity already
	 * exists in the datastore, an exception is thrown.
	 * It uses HTTP POST method.
	 *
	 * @param scrumuser the entity to be inserted.
	 * @return The inserted entity.
	 */
	@ApiMethod(name = "insertScrumUser")
	public ScrumUser insertScrumUser(ScrumUser scrumuser) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (containsScrumUser(scrumuser)) {
				throw new EntityExistsException("Object already exists");
			}
			mgr.makePersistent(scrumuser);
		} finally {
			mgr.close();
		}
		return scrumuser;
	}

	/**
	 * This method is used for updating an existing entity. If the entity does not
	 * exist in the datastore, an exception is thrown.
	 * It uses HTTP PUT method.
	 *
	 * @param scrumuser the entity to be updated.
	 * @return The updated entity.
	 */
	@ApiMethod(name = "updateScrumUser")
	public ScrumUser updateScrumUser(ScrumUser scrumuser) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (!containsScrumUser(scrumuser)) {
				throw new EntityNotFoundException("Object does not exist");
			}
			mgr.makePersistent(scrumuser);
		} finally {
			mgr.close();
		}
		return scrumuser;
	}

	/**
	 * This method removes the entity with primary key id.
	 * It uses HTTP DELETE method.
	 *
	 * @param id the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "removeScrumUser")
	public void removeScrumUser(@Named("id") String id) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			ScrumUser scrumuser = mgr.getObjectById(ScrumUser.class, id);
			mgr.deletePersistent(scrumuser);
		} finally {
			mgr.close();
		}
	}

	private boolean containsScrumUser(ScrumUser scrumuser) {
		PersistenceManager mgr = getPersistenceManager();
		boolean contains = true;
		try {
			mgr.getObjectById(ScrumUser.class, scrumuser.getEmail());
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
