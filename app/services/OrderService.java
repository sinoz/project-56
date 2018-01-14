package services;

import models.Order;
import play.db.Database;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The OrderService that handles orders in the database
 *
 * @author Johan van der Hoeven
 * @author Maurice van Veen
 */
@Singleton
public final class OrderService {
    /**
     * The required {@link Database} dependency to fetch database connections.
     */
    private final play.db.Database database;

    private final ProductService productService;
    private final UserViewService userViewService;

    @Inject
    public OrderService(play.db.Database database, ProductService productService, UserViewService userViewService) {
        this.database = database;
        this.productService = productService;
        this.userViewService = userViewService;
    }

    /**
     * Attempts to find an {@link Order} by id
     */
    public Optional<Order> getOrderByTrackId(String trackid){
        return database.withConnection(connection -> {
            Optional<Order> result = Optional.empty();

            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM orders WHERE trackid=?");
            stmt.setString(1, trackid);

            ResultSet results = stmt.executeQuery();

            if (results.next()){
                result = Optional.of(ModelService.createOrder(results, userViewService, productService));
            }

            return result;
        });
    }

    public List<Order> fetchOrders(){
        return database.withConnection(connection -> {
            List<Order> result = new ArrayList<>();
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM orders");

            ResultSet results = stmt.executeQuery();

            while (results.next()){
                result.add(ModelService.createOrder(results, userViewService, productService));
            }

            return result;
        });
    }

    public List<Order> getOrdersByUser(int userid){
        return database.withConnection(connection -> {
            List<Order> result = new ArrayList<>();
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM orders WHERE userid=?");
            stmt.setInt(1, userid);

            ResultSet results = stmt.executeQuery();

            while (results.next()){
                result.add(ModelService.createOrder(results, userViewService, productService));
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