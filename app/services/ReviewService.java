package services;

import models.Review;
import models.ReviewToken;
import models.User;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * TODO
 *
 * @author Joris Stander
 * @author D.Bakhuis
 */
public final class ReviewService {

    private final play.db.Database database;
    private final UserViewService userViewService;

    /**
     * Creates a new {@link ReviewService}.
     */
    @Inject
    public ReviewService(play.db.Database database, UserViewService userViewService) {
        this.database = database;
        this.userViewService = userViewService;
    }

    public List<Review> fetchReviews(){
        return database.withConnection(connection -> {
            List<Review> list = new ArrayList<>();

            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM reviews");

            ResultSet results = stmt.executeQuery();

            while(results.next()){
                Review r = new Review();

                r.setId(results.getString("id"));
                r.setUserReceiverId(results.getInt("userreceiverid"));
                r.setUserSenderId(results.getInt("usersenderid"));
                r.setTitle(results.getString("title"));
                r.setDescription(results.getString("description"));
                r.setRating(results.getInt("rating"));

                Optional<User> sender = userViewService.fetchUser(r.getUserSenderId());
                sender.ifPresent(r::setSender);
                Optional<User> receiver = userViewService.fetchUser(r.getUserReceiverId());
                receiver.ifPresent(r::setReceiver);

                list.add(r);
            }
            return list;
        });
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
