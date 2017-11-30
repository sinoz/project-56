package services;

import models.Review;

import javax.inject.Inject;
import java.sql.PreparedStatement;

/**
 * TODO
 *
 * @author Joris Stander
 * @author D.Bakhuis 
 */
public final class ReviewService {

    private final play.db.Database database;

    /**
     * Creates a new {@link ReviewService}.
     */
    @Inject
    public ReviewService(play.db.Database database) {
        this.database = database;
    }

    /**
     * Attempts to update a exciting user and returns whether it has successfully updated the settings
     * using the given {@link forms.ReviewForm}.
     */
    public void addReview(Review review) {
        database.withConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO reviews (userreceiverid, usersenderid, title, description, rating) VALUES(?, ?, ?, ?, ?)");
            stmt.setInt(1, 1);
            stmt.setInt(2, 2);
            stmt.setString(3, review.getTitle());
            stmt.setString(4, review.getDescription());
            stmt.setInt(5, 4);
            stmt.execute();
        });
    }
}
