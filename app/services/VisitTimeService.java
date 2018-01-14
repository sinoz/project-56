package services;

import forms.RegistrationForm;
import models.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.*;

/**
 * The UserViewService that retrieves {@link User}s, {@link Product}s, and {@link Review}s
 *
 * @author Johan van der Hoeven
 * @author Maurice van Veen
 */
@Singleton
public final class VisitTimeService {
    private final play.db.Database database;

    @Inject
    public VisitTimeService(play.db.Database database) {
        this.database = database;
    }

    public List<VisitTime> fetchVisitTimes() {
        return database.withConnection(connection -> {
            List<VisitTime> users = new ArrayList<>();

            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM visittimes");

            ResultSet results = stmt.executeQuery();

            while (results.next()) {
                users.add(ModelService.createVisitTime(results));
            }

            return users;
        });
    }

    public void addVisitTime(int userid) {
        database.withConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO visittimes (userid, time) VALUES(?, ?)");
            stmt.setInt(1, userid);
            stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            stmt.execute();
        });
    }

}
