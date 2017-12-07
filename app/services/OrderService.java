package services;

import models.Order;
import play.db.Database;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.UUID;

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
    public Optional<Order> getOrderByTrackId(String trackid){
        return database.withConnection(connection -> {
            Optional<Order> result = Optional.empty();

            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM orders WHERE trackid=?");
            stmt.setString(1, trackid);

            ResultSet queryResult = stmt.executeQuery();

            if(queryResult.next()){
                Order order = new Order();

                order.setId(queryResult.getInt("id"));
                order.setTrackId(trackid);
                order.hasUser(queryResult.getBoolean("hasuser"));
                order.setUserId(queryResult.getInt("userid"));
                order.setProductId(queryResult.getInt("productid"));
                order.setPrice(queryResult.getFloat("price"));
                order.setCouponCode(queryResult.getString("couponcode"));
                order.setOrderType(queryResult.getInt("ordertype"));
                order.setStatus(queryResult.getInt("status"));

                result = Optional.of(order);
            }

            return result;
        });
    }

    public String getNewTrackingId() {
        return UUID.randomUUID().toString();
    }

    public String createVerification(String token, String userId, String sessionToken, String p, String trackingId, String couponCode, String mail) {
        String[] s = new String[] { token, userId, sessionToken, p, trackingId, couponCode, mail };
        return SecurityService.hash(split(s, 0));
    }

    private String split(String[] s, int i) {
        if (i < s.length) {
            String a = s[i];
            return merge(a, split(s, i + 1));
        }
        return "";
    }

    private String merge(String a, String b) {
        return SecurityService.encodePassword(a, b);
    }
}
