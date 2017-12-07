package models;

import io.ebean.Model;
import play.data.format.Formats;
import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * An account order.
 *
 * @author Maurice van Veen
 */
@Entity(name = "order")
public final class Order extends Model {
	/** The primary key of an order. */
	@Id
	private int id;

	/** track id of the order, used by the user for searching. */
	@Constraints.Required
	private String trackId;

	@Constraints.Required
	private boolean hasuser;

	/** id of the user, can be used for searching for user. */
	@Constraints.Required
	private int userId;

	/** The id of the product, can be used for searching for product */
	@Constraints.Required
	private int productId;

	/** price of the order (total price) */
	@Constraints.Required
	private float price;

	/** A coupon code used for this order */
	private String couponCode;

	private int orderType;

	@Constraints.Required
	private int status;

	/** date of when the order was placed, it is only set once */
	@Constraints.Required
	@Formats.DateTime(pattern = "dd/MM/yyyy")
	private Date orderplaced;

	private Product product;
	private ViewableUser user;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTrackId() {
		return trackId;
	}

	public void setTrackId(String trackId) {
		this.trackId = trackId;
	}

	public boolean hasUser() {
		return hasuser;
	}

	public void hasUser(boolean hasuser) {
		this.hasuser = hasuser;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getProductId() {
		return productId;
	}

	public void setProductId(int productId) {
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

	public int getOrderType() {
		return orderType;
	}

	public void setOrderType(int orderType) {
		this.orderType = orderType;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Date getOrderplaced() {
		return orderplaced;
	}

	public void setOrderplaced(Date orderplaced) {
		this.orderplaced = orderplaced;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public ViewableUser getUser() {
		return user;
	}

	public void setUser(ViewableUser user) {
		this.user = user;
	}
}