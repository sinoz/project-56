package models;

import javax.persistence.*;
import io.ebean.*;
import play.data.format.Formats;
import play.data.validation.*;

import java.net.URL;
import java.util.Date;
import java.util.List;

@Entity
public class User extends Model {

    @Id
    private String id;

    // username displayed in the webshop / can be used for log in
    @Constraints.Required
    private String username;

    // password used to log in
    @Constraints.Required
    private String password;

//    TODO: optional
//    @Constraints.Required
//    private String password_salt;

    // mail used to log in
    @Constraints.Required
    private String mail;

    // link to the image as profile picture for the user
    @Constraints.Required
    private URL profilePicture;

    // mail used for payout after a user bought a product with this seller
    @Constraints.Required
    private String paymentMail;

    // list with product ids, used for searching for product (these products are owned by this user)
    private List<String> inventory;

    // list with product ids, used for searching for product (if a product is found that is disabled or not visible in the webshop it should be removed from this list)
    private List<String> favorites;

    // list with order ids, used for searching for order
    private List<String> orderHistory;

    // date of when the user first joined, it is only set once
    @Constraints.Required
    @Formats.DateTime(pattern = "dd/MM/yyyy")
    private Date memberSince;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public URL getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(URL profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getPaymentMail() {
        return paymentMail;
    }

    public void setPaymentMail(String paymentMail) {
        this.paymentMail = paymentMail;
    }

    public List<String> getInventory() {
        return inventory;
    }

    public void setInventory(List<String> inventory) {
        this.inventory = inventory;
    }

    public List<String> getFavorites() {
        return favorites;
    }

    public void setFavorites(List<String> favorites) {
        this.favorites = favorites;
    }

    public List<String> getOrderHistory() {
        return orderHistory;
    }

    public void setOrderHistory(List<String> orderHistory) {
        this.orderHistory = orderHistory;
    }

    public Date getMemberSince() {
        return memberSince;
    }

    public void setMemberSince(Date memberSince) {
        this.memberSince = memberSince;
    }
}
