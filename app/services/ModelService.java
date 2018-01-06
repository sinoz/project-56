package services;

import models.*;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

/**
 * @author Maurice van Veen
 */
final class ModelService {

    static ViewableUser createViewableUser(ResultSet results) throws SQLException {
        int id = results.getInt("id");
        String username = results.getString("username");
        String mail = results.getString("mail");
        String profilepicture = results.getString("profilepicture");
        Date memberSince = results.getDate("membersince");
        return new ViewableUser(id, username, mail, profilepicture, memberSince);
    }

    static User createUser(ResultSet results) throws SQLException {
        User u = new User();
        u.setId(results.getInt("id"));
        u.setUsername(results.getString("username"));
        u.setPassword(results.getString("password"));
        u.setSalt(results.getString("passwordsalt"));
        u.setMail(results.getString("mail"));
        u.setPaymentMail(results.getString("paymentmail"));
        u.setProfilePicture(results.getString("profilepicture"));
        u.setMemberSince(results.getDate("membersince"));

        List<Integer> favorites = new ArrayList<>();
        Array a = results.getArray("favorites");
        if (a != null) {
            favorites.addAll(Arrays.asList((Integer[]) a.getArray()));
        }
        u.setFavorites(favorites);
        return u;
    }

    static CouponCode createCouponCode(ResultSet results) throws SQLException {
        String code = results.getString("code");
        double percentage = results.getDouble("percentage");
        return new CouponCode(code, percentage);
    }

    static Product createProduct(ResultSet results, UserViewService userViewService, ProductService productService) throws SQLException {
        Product product = new Product();
        product.setId(results.getInt("id"));
        product.setUserId(results.getInt("userid"));
        product.setGameId(results.getInt("gameid"));
        product.setVisible(results.getBoolean("visible"));
        product.setDisabled(results.getBoolean("disabled"));
        product.setTitle(results.getString("title"));
        product.setDescription(results.getString("description"));
        product.setAddedSince(results.getDate("addedsince"));
        product.setCanBuy(results.getBoolean("canbuy"));
        product.setBuyPrice(results.getDouble("buyprice"));
        product.setCanTrade(results.getBoolean("cantrade"));
        product.setMailLast(results.getString("maillast"));
        product.setMailCurrent(results.getString("mailcurrent"));
        product.setPasswordCurrent(results.getString("passwordcurrent"));

        Optional<User> user = userViewService.fetchUser(product.getUserId());
        user.ifPresent(product::setUser);

        Optional<GameCategory> gameCategory = productService.fetchGameCategory(product.getGameId());
        gameCategory.ifPresent(product::setGameCategory);

        return product;
    }

    static GameCategory createGameCategory(ResultSet results) throws SQLException {
        GameCategory gameCategory = new GameCategory();
        gameCategory.setId(results.getInt("id"));
        gameCategory.setName(results.getString("name"));
        gameCategory.setImage(results.getString("image"));
        gameCategory.setDescription(results.getString("description"));
        gameCategory.setGenre(results.getString("genre"));
        gameCategory.setSearch(results.getInt("search"));
        return gameCategory;
    }

    static Order createOrder(ResultSet results, UserViewService userViewService, ProductService productService) throws SQLException {
        Order order = new Order();

        int productid = results.getInt("productid");
        int userid = results.getInt("userid");

        order.setId(results.getInt("id"));
        order.setTrackId(results.getString("trackid"));
        order.hasUser(results.getBoolean("hasuser"));
        order.setUserId(userid);
        order.setProductId(productid);
        order.setPrice(results.getFloat("price"));
        order.setCouponCode(results.getString("couponcode"));
        order.setOrderType(results.getInt("ordertype"));
        order.setStatus(results.getInt("status"));
        order.setOrderplaced(results.getDate("orderplaced"));

        Optional<ViewableUser> user = userViewService.fetchViewableUser(userid);
        Optional<Product> product = productService.fetchProduct(productid);

        product.ifPresent(order::setProduct);
        user.ifPresent(order::setUser);
        return order;
    }

    static Review createReview(ResultSet results, UserViewService userViewService) throws SQLException {
        Review review = new Review();
        review.setId(results.getString("id"));
        review.setUserReceiverId(results.getInt("userreceiverid"));
        review.setUserSenderId(results.getInt("usersenderid"));
        review.setTitle(results.getString("title"));
        review.setDescription(results.getString("description"));
        review.setRating(results.getInt("rating"));

        Optional<User> sender = userViewService.fetchUser(review.getUserSenderId());
        sender.ifPresent(review::setSender);
        Optional<User> receiver = userViewService.fetchUser(review.getUserReceiverId());
        receiver.ifPresent(review::setReceiver);
        return review;
    }

    static ReviewToken createReviewToken(ResultSet results) throws SQLException {
        ReviewToken reviewToken = new ReviewToken();
        reviewToken.setReviewID(results.getString("reviewid"));
        reviewToken.setUserReceiverId(results.getInt("userreceiverid"));
        reviewToken.setUserSenderId(results.getInt("usersenderid"));
        reviewToken.setProductId(results.getInt("productid"));
        return reviewToken;
    }

    static VisitTime createVisitTime(ResultSet results) throws SQLException {
        int id = results.getInt("id");
        int userid = results.getInt("userid");
        Timestamp time = results.getTimestamp("time");
        return new VisitTime(id, userid, time);
    }
}