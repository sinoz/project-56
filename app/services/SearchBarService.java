package services;

import play.db.Database;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * The SearchBarService with the fetchGameCategoryNames method that retrieves all GameCategoryNames for the searchbar
 *
 * @author Johan van der Hoeven
 */
public final class SearchBarService {
    @Inject private static play.db.Database database;

    public static List<String> fetchGameCategoryNames(){
        return database.withConnection(connection -> {
            List<String> res = new ArrayList<>();

            PreparedStatement stmt = connection.prepareStatement("SELECT name FROM gamecategories");
            ResultSet results = stmt.executeQuery();

            while(results.next()){
                res.add(results.getString("name"));
            }

            return res;
        });
    }
}
