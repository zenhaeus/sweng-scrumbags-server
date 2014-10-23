package ch.epfl.entity;

import ch.epfl.scrumtool.EMF;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.datanucleus.query.JPACursorHelper;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.persistence.EntityManager;
import javax.persistence.Query;

@Api(name = "issueendpoint", namespace = @ApiNamespace(ownerDomain = "epfl.ch", ownerName = "epfl.ch", packagePath = "entity"))
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

		EntityManager mgr = null;
		Cursor cursor = null;
		List<Issue> execute = null;

		try {
			mgr = getEntityManager();
			Query query = mgr.createQuery("select from Issue as Issue");
			if (cursorString != null && cursorString != "") {
				cursor = Cursor.fromWebSafeString(cursorString);
				query.setHint(JPACursorHelper.CURSOR_HINT, cursor);
			}

			if (limit != null) {
				query.setFirstResult(0);
				query.setMaxResults(limit);
			}

			execute = (List<Issue>) query.getResultList();
			cursor = JPACursorHelper.getCursor(execute);
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
		EntityManager mgr = getEntityManager();
		Issue issue = null;
		try {
			issue = mgr.find(Issue.class, id);
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
		EntityManager mgr = getEntityManager();
		try {
			if (containsIssue(issue)) {
				throw new EntityExistsException("Object already exists");
			}
			mgr.persist(issue);
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
		EntityManager mgr = getEntityManager();
		try {
			if (!containsIssue(issue)) {
				throw new EntityNotFoundException("Object does not exist");
			}
			mgr.persist(issue);
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
		EntityManager mgr = getEntityManager();
		try {
			Issue issue = mgr.find(Issue.class, id);
			mgr.remove(issue);
		} finally {
			mgr.close();
		}
	}

	private boolean containsIssue(Issue issue) {
		EntityManager mgr = getEntityManager();
		boolean contains = true;
		try {
			Issue item = mgr.find(Issue.class, issue.getKey());
			if (item == null) {
				contains = false;
			}
		} finally {
			mgr.close();
		}
		return contains;
	}

	private static EntityManager getEntityManager() {
		return EMF.get().createEntityManager();
	}

}
