package services;

import models.GameCategory;
import models.Product;
import models.Review;
import models.User;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class UserViewService {
    private final play.db.Database database;

    @Inject
    public UserViewService(play.db.Database database){
        this.database = database;
    }

    /**
     * Attempts to find a {@link User} that matches the given id.
     */
    private Optional<User> getUser(int id) {
        return database.withConnection(connection -> {
            Optional<User> user = Optional.empty();

            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE id=?");
            stmt.setInt(1, id);

            ResultSet results = stmt.executeQuery();

            if (results.next()) {
                User u = new User();

                u.setId(results.getString("id"));
                u.setUsername(results.getString("username"));
                u.setProfilePicture(results.getString("profilepicture"));

                user = Optional.of(u);
            }

            return user;
        });
    }

    /**
     * Attempts to get the {@link Product}s that belong to the given userId.
     */
    public List<List<Product>> getUserProducts(int userId) {
        return database.withConnection(connection -> {
            Optional<User> user = getUser(userId);
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
                r.setSender(getUser(senderid).get());

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
