package services;

import forms.ReviewForm;

import javax.inject.Inject;
import java.sql.PreparedStatement;

/**
 * TODO
 *
 * @author Joris Stander
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
    public void addReview(ReviewForm form) {
        database.withConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO reviews (userreceiverid, usersenderid, title, description, rating) VALUES(?, ?, ?, ?, ?)");
            stmt.setString(1, "1");
            stmt.setString(2, "2");
            stmt.setString(3, form.Title);
            stmt.setString(4, form.description);
            stmt.setString(5, Integer.toString(form.rating));
            stmt.execute();
        });
    }
}
