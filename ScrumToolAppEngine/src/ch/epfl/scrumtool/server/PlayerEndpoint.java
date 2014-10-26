package ch.epfl.scrumtool.server;

import ch.epfl.scrumtool.server.Constants;
import ch.epfl.scrumtool.server.EMF;

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

@Api(
    name = "scrumtool",
    version = "v1",
    namespace = @ApiNamespace(ownerDomain = "epfl.ch", ownerName = "epfl.ch", packagePath = "scrumtool.server"),
    clientIds = {Constants.ANDROID_CLIENT_ID},
    audiences = {Constants.ANDROID_AUDIENCE}
    )


public class PlayerEndpoint {
    
	/**
	 * This method lists all the entities inserted in datastore.
	 * It uses HTTP GET method and paging support.
	 *
	 * @return A CollectionResponse class containing the list of all entities
	 * persisted and a cursor to the next page.
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@ApiMethod(name = "listPlayer")
	public CollectionResponse<Player> listPlayer(
			@Nullable @Named("cursor") String cursorString,
			@Nullable @Named("limit") Integer limit) {

		EntityManager mgr = null;
		Cursor cursor = null;
		List<Player> execute = null;

		try {
			mgr = getEntityManager();
			Query query = mgr.createQuery("select from Player as Player");
			if (cursorString != null && cursorString != "") {
				cursor = Cursor.fromWebSafeString(cursorString);
				query.setHint(JPACursorHelper.CURSOR_HINT, cursor);
			}

			if (limit != null) {
				query.setFirstResult(0);
				query.setMaxResults(limit);
			}

			execute = (List<Player>) query.getResultList();
			cursor = JPACursorHelper.getCursor(execute);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();

			// Tight loop for fetching all entities from datastore and accomodate
			// for lazy fetch.
			for (Player obj : execute)
				;
		} finally {
			mgr.close();
		}

		return CollectionResponse.<Player> builder().setItems(execute)
				.setNextPageToken(cursorString).build();
	}

	/**
	 * This method gets the entity having primary key id. It uses HTTP GET method.
	 *
	 * @param id the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getPlayer")
	public Player getPlayer(@Named("id") Long id) {
		EntityManager mgr = getEntityManager();
		Player player = null;
		try {
			player = mgr.find(Player.class, id);
		} finally {
			mgr.close();
		}
		return player;
	}

	/**
	 * This inserts a new entity into App Engine datastore. If the entity already
	 * exists in the datastore, an exception is thrown.
	 * It uses HTTP POST method.
	 *
	 * @param player the entity to be inserted.
	 * @return The inserted entity.
	 */
	@ApiMethod(name = "insertPlayer")
	public Player insertPlayer(Player player) {
		EntityManager mgr = getEntityManager();
		try {
			if (containsPlayer(player)) {
				throw new EntityExistsException("Object already exists");
			}
			mgr.persist(player);
		} finally {
			mgr.close();
		}
		return player;
	}

	/**
	 * This method is used for updating an existing entity. If the entity does not
	 * exist in the datastore, an exception is thrown.
	 * It uses HTTP PUT method.
	 *
	 * @param player the entity to be updated.
	 * @return The updated entity.
	 */
	@ApiMethod(name = "updatePlayer")
	public Player updatePlayer(Player player) {
		EntityManager mgr = getEntityManager();
		try {
			if (!containsPlayer(player)) {
				throw new EntityNotFoundException("Object does not exist");
			}
			mgr.persist(player);
		} finally {
			mgr.close();
		}
		return player;
	}

	/**
	 * This method removes the entity with primary key id.
	 * It uses HTTP DELETE method.
	 *
	 * @param id the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "removePlayer")
	public void removePlayer(@Named("id") Long id) {
		EntityManager mgr = getEntityManager();
		try {
			Player player = mgr.find(Player.class, id);
			mgr.remove(player);
		} finally {
			mgr.close();
		}
	}

	private boolean containsPlayer(Player player) {
		EntityManager mgr = getEntityManager();
		boolean contains = true;
		try {
			Player item = mgr.find(Player.class, player.getKey());
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
