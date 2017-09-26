package models;

import io.ebean.Model;
import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * An account order.
 *
 * @author Maurice van Veen
 */
@Entity(name = "order")
public final class Order extends Model {
	/** The primary key of an order. */
	@Id
	private String id;

	/** id of the user, can be used for searching for user. */
	@Constraints.Required
	private String userId;

	/** The id of the product, can be used for searching for product */
	@Constraints.Required
	private String productId;

	/** price of the order (total price) */
	@Constraints.Required
	private float price;

	/** A coupon code used for this order */
	private String couponCode;

	/**
	 * The status of this order (0-5)
	 * 0:  added
	 * 1:  processing
	 * 2:  changing email
	 * 3:  changing password
	 * 4:  initializing inventory
	 * 5:  done
	 * TODO: use an Enum for this instead of a hardcoded int value
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