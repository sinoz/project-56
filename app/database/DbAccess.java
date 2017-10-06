package database;

import com.github.davidmoten.rx.jdbc.Database;
import com.github.davidmoten.rx.jdbc.QuerySelect;

import javax.inject.Inject;
import java.util.function.Function;

/** A wrapper in between {@link play.db.Database} and {@link com.github.davidmoten.rx.jdbc.Database}
 * to connect the two and allow easy injection.
 * @author I.A
 */
public final class DbAccess {
	/**
	 * The required {@link play.db.Database} dependency.
	 */
	private final play.db.Database db;

	/**
	 * Creates a new {@link DbAccess}.
	 */
	@Inject
	public DbAccess(play.db.Database db) {
		this.db = db;
	}

	/**
	 * Performs a select query, returning a {@link QuerySelect.Builder} to continue building the query.
	 */
	public QuerySelect.Builder select(String query) {
		return withConnection(connection -> connection.select(query));
	}

	public <T> T withConnection(Function<Database, T> call) {
		return db.withConnection(connection -> {
			return call.apply(Database.from(connection));
		});
	}
}
