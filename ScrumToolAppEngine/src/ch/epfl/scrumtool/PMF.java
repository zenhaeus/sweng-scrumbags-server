package ch.epfl.scrumtool;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

/**
 * 
 * @author aschneuw, GoogleAppEngine
 *
 */
public final class PMF {
	private static final PersistenceManagerFactory PMF_INSTANCE = JDOHelper
			.getPersistenceManagerFactory("transactions-optional");

	private PMF() {
	}

	public static PersistenceManagerFactory get() {
		return PMF_INSTANCE;
	}
}