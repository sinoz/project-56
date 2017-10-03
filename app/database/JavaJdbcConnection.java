package database;

import play.db.Database;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public final class JavaJdbcConnection {
    @Inject private Database db;

    public void tryIt() {
        db.withConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM actor ORDER BY actor_id ASC");
            ResultSet results = stmt.executeQuery();
            while (results.next()) {
                System.out.println(results.getString("first_name"));
            }
        });
    }
}