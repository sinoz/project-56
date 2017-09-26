package models;

import javax.persistence.*;
import io.ebean.*;
import play.data.validation.*;

@Entity
public class Order extends Model {

    @Id
    private String id;

    // id of the user, can be used for searching for user
    @Constraints.Required
    private String userId;

    // id of the product, can be used for searching for product
    @Constraints.Required
    private String productId;

    // price of the order (total price)
    @Constraints.Required
    private float price;

    // coupon code used for this order
    private String couponCode;

    /*
    status of the order (0-5)
    0:  added
    1:  processing
    2:  changing email
    3:  changing password
    4:  initializing inventory
    5:  done
     */
    @Constraints.Required
    private int status;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }
}