package services;

import models.Product;
import models.Review;
import models.User;
import models.ViewableUser;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The UserViewService that retrieves {@link User}s, {@link Product}s, and {@link Review}s
 *
 * @author Johan van der Hoeven
 * @author Maurice van Veen
 */
@Singleton
public final class UserViewService {
    private final play.db.Database database;

    @Inject
    public UserViewService(play.db.Database database) {
        this.database = database;
    }

    /**
     * Attempts to find all {@link ViewableUser}.
     */
    public List<ViewableUser> fetchViewableUsers() {
        return database.withConnection(connection -> {
            List<ViewableUser> users = new ArrayList<>();

            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users");

            ResultSet results = stmt.executeQuery();

            while (results.next()) {
                users.add(ModelService.createViewableUser(results));
            }

            return users;
        });
    }

    /**
     * Attempts to find a {@link ViewableUser} that matches the given id.
     */
    public Optional<ViewableUser> fetchViewableUser(int id) {
        return database.withConnection(connection -> {
            Optional<ViewableUser> user = Optional.empty();

            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE id=?");
            stmt.setInt(1, id);

            ResultSet results = stmt.executeQuery();

            if (results.next()) {
                user = Optional.of(ModelService.createViewableUser(results));
            }

            return user;
        });
    }

    /**
     * Attempts to find a {@link ViewableUser} that matches the given username.
     */
    public Optional<ViewableUser> fetchViewableUser(String username) {
        return database.withConnection(connection -> {
            Optional<ViewableUser> user = Optional.empty();

            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE username=?");
            stmt.setString(1, username);

            ResultSet results = stmt.executeQuery();

            if (results.next()) {
                user = Optional.of(ModelService.createViewableUser(results));
            }

            return user;
        });
    }

    /**
     * Attempts to find a {@link User} that matches the given username.
     */
    public Optional<User> fetchUser(String username) {
        return database.withConnection(connection -> {
            Optional<User> user = Optional.empty();

            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE username=?");
            stmt.setString(1, username);

            ResultSet results = stmt.executeQuery();

            if (results.next()) {
                user = Optional.of(ModelService.createUser(results));
            }

            return user;
        });
    }

    /**
     * Attempts to find a {@link User} that matches the given id.
     */
    public Optional<User> fetchUser(int id) {
        return database.withConnection(connection -> {
            Optional<User> user = Optional.empty();

            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE id=?");
            stmt.setInt(1, id);

            ResultSet results = stmt.executeQuery();

            if (results.next()) {
                user = Optional.of(ModelService.createUser(results));
            }

            return user;
        });
    }

    /**
     * Attempts to get the {@link Review}s that belong to the given userId.
     */
    public List<Review> fetchUserReviews(int userId){
        return database.withConnection(connection -> {
            List<Review> list = new ArrayList<>();

            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM reviews WHERE userreceiverid=?");
            stmt.setInt(1, userId);

            ResultSet results = stmt.executeQuery();

            while(results.next()){
                list.add(ModelService.createReview(results, this));
            }
            return list;
        });
    }

    /**
     * Attempts to get the {@link Product}s that belong to the given userId.
     */
    public boolean fetchProductIsFavourited(int userId, int productId) {
        Optional<User> user = fetchUser(userId);//
        return user.isPresent() && user.get().getFavorites() != null && user.get().getFavorites().contains(productId);

    }

    /**
     * Attempts to get the {@link Review}s that belong to the given userId.
     */
    public List<List<Review>> getUserReviews(int userId){
        return database.withConnection(connection -> {
            List<List<Review>> list = new ArrayList<>();

            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM reviews WHERE userreceiverid=?");
            stmt.setInt(1, userId);

            ResultSet results = stmt.executeQuery();

            List<Review> row = new ArrayList<>();
            int l = 0;
            while(results.next()){
                row.add(ModelService.createReview(results, this));
                l++;
                if(l > 1) {
                    list.add(row);
                    row = new ArrayList<>();
                    l = 0;
                }
            }
            if(l > 0) list.add(row);

            return list;
        });
    }
}