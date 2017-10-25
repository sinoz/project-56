package services;

import models.*;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * The UserViewService that retrieves {@link User}s, {@link Product}s, and {@link Review}s for the {@link controllers.UserAccountController}
 *
 * @author Johan van der Hoeven
 * @author Maurice van Veen
 */
public final class UserViewService {
    private final play.db.Database database;

    @Inject
    public UserViewService(play.db.Database database){
        this.database = database;
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
                String username = results.getString("username");
                String profilepicture = results.getString("profilepicture");
                Date memberSince = results.getDate("membersince");
                ViewableUser u = new ViewableUser(id, username, profilepicture, memberSince);

                user = Optional.of(u);
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
                int id = results.getInt("id");
                String profilepicture = results.getString("profilepicture");
                Date memberSince = results.getDate("membersince");
                ViewableUser u = new ViewableUser(id, username, profilepicture, memberSince);

                user = Optional.of(u);
            }

            return user;
        });
    }

    /**
     * Attempts to find a {@link User} that matches the given username and password combination.
     */
    public Optional<User> fetchUser(int id) {
        return database.withConnection(connection -> {
            Optional<User> user = Optional.empty();

            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE id=?");
            stmt.setInt(1, id);

            ResultSet results = stmt.executeQuery();

            if (results.next()) {
                User u = new User();

                u.setId(results.getString("id"));
                u.setUsername(results.getString("username"));
                u.setPassword(results.getString("password"));
                u.setMail(results.getString("mail"));
                u.setPaymentMail(results.getString("paymentmail"));
                u.setProfilePicture(results.getString("profilepicture"));
                u.setMemberSince(results.getDate("membersince"));

                user = Optional.of(u);
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
                Review r = new Review();

                r.setId(results.getString("id"));
                r.setUserReceiverId(results.getInt("userreceiverid"));
                r.setUserSenderId(results.getInt("usersenderid"));
                r.setTitle(results.getString("title"));
                r.setDescription(results.getString("description"));
                r.setRating(results.getInt("rating"));

                Optional<User> sender = fetchUser(r.getUserSenderId());
                sender.ifPresent(r::setSender);
                Optional<User> receiver = fetchUser(r.getUserReceiverId());
                receiver.ifPresent(r::setReceiver);

                list.add(r);
            }
            return list;
        });
    }

    /**
     * Attempts to get the {@link Product}s that belong to the given userId.
     */
    public List<List<Product>> fetchUserProducts(int userId) {
        return database.withConnection(connection -> {
            Optional<User> user = fetchUser(userId);
            List<List<Product>> list = new ArrayList<>();

            if (user.isPresent()) {
                PreparedStatement stmt = connection.prepareStatement("SELECT * FROM gameaccounts WHERE userid=? AND visible=TRUE AND disabled=FALSE");
                stmt.setInt(1, userId);

                ResultSet results = stmt.executeQuery();

                List<Product> row = new ArrayList<>();
                int l = 0;
                while (results.next()) {
                    Product p = new Product();

                    p.setId(results.getInt("id"));
                    p.setUserId(results.getInt("userid"));
                    p.setGameId(results.getInt("gameid"));
                    p.setTitle(results.getString("title"));
                    p.setDescription(results.getString("description"));
                    p.setAddedSince(results.getDate("addedsince"));
                    p.setCanBuy(results.getBoolean("canbuy"));
                    p.setBuyPrice(results.getDouble("buyprice"));
                    p.setCanTrade(results.getBoolean("cantrade"));

                    p.setUser(user.get());

                    Optional<GameCategory> gameCategory = fetchGameCategory(p.getGameId());
                    gameCategory.ifPresent(p::setGameCategory);

                    row.add(p);
                    l++;
                    if (l > 1) {
                        list.add(row);
                        row = new ArrayList<>();
                        l = 0;
                    }
                }
                if (l > 0) list.add(row);
            }

            return list;
        });
    }

    /**
     * Attempts to find a {@link GameCategory} that matches the given game id.
     */
    private Optional<GameCategory> fetchGameCategory(int id) {
        return database.withConnection(connection -> {
            Optional<GameCategory> gameCategory = Optional.empty();

            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM gamecategories WHERE id=?");
            stmt.setInt(1, id);

            ResultSet results = stmt.executeQuery();

            if (results.next()) {
                GameCategory gc = new GameCategory();

                gc.setId(results.getInt("id"));
                gc.setName(results.getString("name"));
                gc.setImage(results.getString("image"));
                gc.setDescription(results.getString("description"));

                gameCategory = Optional.of(gc);
            }

            return gameCategory;
        });
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
                Review r = new Review();

                r.setId(results.getString("id"));
                r.setUserReceiverId(results.getInt("userreceiverid"));
                r.setUserSenderId(results.getInt("usersenderid"));
                r.setTitle(results.getString("title"));
                r.setDescription(results.getString("description"));
                r.setRating(results.getInt("rating"));
                int senderid = results.getInt("usersenderid");
                Optional<User> user = fetchUser(senderid);
                user.ifPresent(r::setSender);

                row.add(r);
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
