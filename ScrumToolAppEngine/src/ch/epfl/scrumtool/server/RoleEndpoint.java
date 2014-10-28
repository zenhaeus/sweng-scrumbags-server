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

@Api(name = "roleendpoint", namespace = @ApiNamespace(ownerDomain = "epfl.ch", ownerName = "epfl.ch", packagePath = "scrumtool.server"))
public class RoleEndpoint {

	/**
	 * This method lists all the entities inserted in datastore.
	 * It uses HTTP GET method and paging support.
	 *
	 * @return A CollectionResponse class containing the list of all entities
	 * persisted and a cursor to the next page.
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@ApiMethod(name = "listRole")
	public CollectionResponse<Role> listRole(
			@Nullable @Named("cursor") String cursorString,
			@Nullable @Named("limit") Integer limit) {

		PersistenceManager mgr = null;
		Cursor cursor = null;
		List<Role> execute = null;

		try {
			mgr = getPersistenceManager();
			Query query = mgr.newQuery(Role.class);
			if (cursorString != null && cursorString != "") {
				cursor = Cursor.fromWebSafeString(cursorString);
				HashMap<String, Object> extensionMap = new HashMap<String, Object>();
				extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
				query.setExtensions(extensionMap);
			}

			if (limit != null) {
				query.setRange(0, limit);
			}

			execute = (List<Role>) query.execute();
			cursor = JDOCursorHelper.getCursor(execute);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();

			// Tight loop for fetching all entities from datastore and accomodate
			// for lazy fetch.
			for (Role obj : execute)
				;
		} finally {
			mgr.close();
		}

		return CollectionResponse.<Role> builder().setItems(execute)
				.setNextPageToken(cursorString).build();
	}

	/**
	 * This method gets the entity having primary key id. It uses HTTP GET method.
	 *
	 * @param id the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getRole")
	public Role getRole(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		Role role = null;
		try {
			role = mgr.getObjectById(Role.class, id);
		} finally {
			mgr.close();
		}
		return role;
	}

	/**
	 * This inserts a new entity into App Engine datastore. If the entity already
	 * exists in the datastore, an exception is thrown.
	 * It uses HTTP POST method.
	 *
	 * @param role the entity to be inserted.
	 * @return The inserted entity.
	 */
	@ApiMethod(name = "insertRole")
	public Role insertRole(Role role) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (containsRole(role)) {
				throw new EntityExistsException("Object already exists");
			}
			mgr.makePersistent(role);
		} finally {
			mgr.close();
		}
		return role;
	}

	/**
	 * This method is used for updating an existing entity. If the entity does not
	 * exist in the datastore, an exception is thrown.
	 * It uses HTTP PUT method.
	 *
	 * @param role the entity to be updated.
	 * @return The updated entity.
	 */
	@ApiMethod(name = "updateRole")
	public Role updateRole(Role role) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (!containsRole(role)) {
				throw new EntityNotFoundException("Object does not exist");
			}
			mgr.makePersistent(role);
		} finally {
			mgr.close();
		}
		return role;
	}

	/**
	 * This method removes the entity with primary key id.
	 * It uses HTTP DELETE method.
	 *
	 * @param id the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "removeRole")
	public void removeRole(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			Role role = mgr.getObjectById(Role.class, id);
			mgr.deletePersistent(role);
		} finally {
			mgr.close();
		}
	}

	private boolean containsRole(Role role) {
		PersistenceManager mgr = getPersistenceManager();
		boolean contains = true;
		try {
			mgr.getObjectById(Role.class, role.getKey());
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
