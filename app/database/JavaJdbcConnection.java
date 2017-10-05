package database;

import models.GameCategory;
import models.User;
import play.db.Database;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public final class JavaJdbcConnection {
    @Inject private Database db;

    public void tryIt() {
        db.withConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM gamecategories ORDER BY id ASC");
            ResultSet results = stmt.executeQuery();
            while (results.next()) {
                System.out.println(results.getString("name"));
            }
            System.out.println("...Connected");
        });
    }

    public List<GameCategory> getGameCategories() {
        ArrayList<GameCategory> gameCategories = new ArrayList<>();
        db.withConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM gamecategories ORDER BY id ASC");
            ResultSet results = stmt.executeQuery();
            while (results.next()) {
                GameCategory gameCategory = new GameCategory();
                gameCategory.setId(results.getString("id"));
                gameCategory.setName(results.getString("name"));
                gameCategory.setImage(results.getString("image"));
                gameCategory.setDescription(results.getString("description"));
                gameCategories.add(gameCategory);
            }
            return gameCategories;
        });
        return gameCategories;
    }

    public User getUser(){
        User user = new User();
        db.withConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE username='default'");
            ResultSet results = stmt.executeQuery();
            while (results.next()) {
                user.setId(results.getString("id"));
                user.setUsername(results.getString("username"));
                user.setPassword(results.getString("password"));
                user.setMail(results.getString("mail"));
                user.setProfilePicture(results.getString("profilepicture"));
            }
            return user;
        });
        return user;
    }

    public User getUser(String username, String password){
        User user = new User();
        db.withConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE username=" + username + " AND password=" + password);
            ResultSet results = stmt.executeQuery();
            while (results.next()) {
                user.setId(results.getString("id"));
                user.setUsername(results.getString("username"));
                user.setPassword(results.getString("password"));
                user.setMail(results.getString("mail"));
                user.setProfilePicture(results.getString("profilepicture"));
            }
            return user;
        });
        return user;
    }

}