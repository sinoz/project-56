package services;

import models.Order;
import play.db.Database;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

/**
 * The OrderService that handles orders in the database
 *
 * @author Johan van der Hoeven
 */
@Singleton
public final class OrderService {
    /**
     * The required {@link Database} dependency to fetch database connections.
     */
    private final play.db.Database database;

    @Inject
    public OrderService(play.db.Database database){
        this.database = database;
    }

    /**
     * Attempts to find an {@link Order} by id
     */
    public Optional<Order> getOrderById(int id){
        return database.withConnection(connection -> {
            Optional<Order> result = Optional.empty();

            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM orders WHERE id=?");
            stmt.setInt(1, id);

            ResultSet queryResult = stmt.executeQuery();

            if(queryResult.next()){
                Order order = new Order();

                order.setId(queryResult.getInt("id"));
                order.setUserId(queryResult.getInt("userid"));
                order.setProductId(queryResult.getInt("productid"));
                order.setPrice(queryResult.getFloat("price"));
                order.setCouponCode(queryResult.getString("couponcode"));
                order.setStatus(queryResult.getInt("status"));

                result = Optional.of(order);
            }

            return result;
        });
    }
}
