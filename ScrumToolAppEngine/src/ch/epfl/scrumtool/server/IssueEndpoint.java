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
public class IssueEndpoint {

	/**
	 * This method lists all the entities inserted in datastore.
	 * It uses HTTP GET method and paging support.
	 *
	 * @return A CollectionResponse class containing the list of all entities
	 * persisted and a cursor to the next page.
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@ApiMethod(name = "listIssue")
	public CollectionResponse<Issue> listIssue(
			@Nullable @Named("cursor") String cursorString,
			@Nullable @Named("limit") Integer limit) {

		PersistenceManager mgr = null;
		Cursor cursor = null;
		List<Issue> execute = null;

		try {
			mgr = getPersistenceManager();
			Query query = mgr.newQuery(Issue.class);
			if (cursorString != null && cursorString != "") {
				cursor = Cursor.fromWebSafeString(cursorString);
				HashMap<String, Object> extensionMap = new HashMap<String, Object>();
				extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
				query.setExtensions(extensionMap);
			}

			if (limit != null) {
				query.setRange(0, limit);
			}

			execute = (List<Issue>) query.execute();
			cursor = JDOCursorHelper.getCursor(execute);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();

			// Tight loop for fetching all entities from datastore and accomodate
			// for lazy fetch.
			for (Issue obj : execute)
				;
		} finally {
			mgr.close();
		}

		return CollectionResponse.<Issue> builder().setItems(execute)
				.setNextPageToken(cursorString).build();
	}

	/**
	 * This method gets the entity having primary key id. It uses HTTP GET method.
	 *
	 * @param id the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getIssue")
	public Issue getIssue(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		Issue issue = null;
		try {
			issue = mgr.getObjectById(Issue.class, id);
		} finally {
			mgr.close();
		}
		return issue;
	}

	/**
	 * This inserts a new entity into App Engine datastore. If the entity already
	 * exists in the datastore, an exception is thrown.
	 * It uses HTTP POST method.
	 *
	 * @param issue the entity to be inserted.
	 * @return The inserted entity.
	 */
	@ApiMethod(name = "insertIssue")
	public Issue insertIssue(Issue issue) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (containsIssue(issue)) {
				throw new EntityExistsException("Object already exists");
			}
			mgr.makePersistent(issue);
		} finally {
			mgr.close();
		}
		return issue;
	}

	/**
	 * This method is used for updating an existing entity. If the entity does not
	 * exist in the datastore, an exception is thrown.
	 * It uses HTTP PUT method.
	 *
	 * @param issue the entity to be updated.
	 * @return The updated entity.
	 */
	@ApiMethod(name = "updateIssue")
	public Issue updateIssue(Issue issue) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (!containsIssue(issue)) {
				throw new EntityNotFoundException("Object does not exist");
			}
			mgr.makePersistent(issue);
		} finally {
			mgr.close();
		}
		return issue;
	}

	/**
	 * This method removes the entity with primary key id.
	 * It uses HTTP DELETE method.
	 *
	 * @param id the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "removeIssue")
	public void removeIssue(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			Issue issue = mgr.getObjectById(Issue.class, id);
			mgr.deletePersistent(issue);
		} finally {
			mgr.close();
		}
	}

	private boolean containsIssue(Issue issue) {
		PersistenceManager mgr = getPersistenceManager();
		boolean contains = true;
		try {
			mgr.getObjectById(Issue.class, issue.getKey());
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
