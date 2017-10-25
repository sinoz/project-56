package services;

import play.db.Database;

import javax.inject.Inject;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * The UserViewService that retrieves Favourites
 *
 * @author Johan van der Hoeven
 */
public final class FavouritesService{
    /**
     * The required {@link Database} dependency to fetch database connections.
     */
    private final play.db.Database database;

    @Inject
    public FavouritesService(play.db.Database database){
        this.database = database;
    }

    /**
     * The method that adds/deletes a game to a users favourites in the database
     */
    public void add(String prodId, String username){
        Integer productId = Integer.valueOf(prodId);

        database.withConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement("SELECT favorites FROM users WHERE username=?");
            stmt.setString(1, username);

            ResultSet results = stmt.executeQuery();

            if(results.next()){
                List<Integer> list = new ArrayList<>();

                Array a = results.getArray("favorites");
                if(a != null){
                    for(Integer i : (Integer[]) a.getArray()){
                        list.add(i);
                    }
                }

                if(list.contains(productId)){
                    list.remove(productId);
                } else {
                    list.add(productId);
                }
                Array sqlArray = connection.createArrayOf("INTEGER", list.toArray());

                stmt = connection.prepareStatement("UPDATE users SET favorites=? WHERE username=?");
                stmt.setArray(1, sqlArray);
                stmt.setString(2, username);

                stmt.execute();
            }
        });
    }
}
