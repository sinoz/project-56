package services;

import models.Review;
import models.ReviewToken;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

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
            stmt.setInt(1, review.getUserReceiverId());
            stmt.setInt(2, review.getUserSenderId());
            stmt.setString(3, review.getTitle());
            stmt.setString(4, review.getDescription());
            stmt.setInt(5, review.getRating());
            stmt.execute();
        });
    }

    public void disableReviewToken(String reviewid) {
        database.withConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM reviewtokens WHERE reviewid=?");
            stmt.setString(1, reviewid);
            stmt.execute();
        });
    }

    public Optional<ReviewToken> getReviewByToken(String reviewid) {
        return database.withConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM reviewtokens WHERE reviewid=?");
            stmt.setString(1, reviewid);

            Optional<ReviewToken> reviewToken = Optional.empty();

            ResultSet result = stmt.executeQuery();

            if (result.next()) {
                ReviewToken review = new ReviewToken();
                review.setReviewID(reviewid);
                review.setUserReceiverId(result.getInt("userreceiverid"));
                review.setUserSenderId(result.getInt("usersenderid"));
                review.setProductId(result.getInt("productid"));

                reviewToken = Optional.of(review);
            }

            return reviewToken;
        });
    }
}
