package models;

import io.ebean.Model;
import play.data.format.Formats;
import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * A product.
 *
 * @author: Maurice van Veen
 */
@Entity(name = "product")
public final class Product extends Model {
	@Id
	private String id;

	/** id of the seller/user, can be used for searching for seller/user */
	@Constraints.Required
	private String userId;

	/** id of the game, can be used for searching for game category */
	@Constraints.Required
	private String gameId;

	/** if the product is displayed/visible in webshop */
	@Constraints.Required
	private boolean visible;

	/** if the product is disabled from webshop & inventory (is used for order history etc.) */
	@Constraints.Required
	private boolean disabled;

	/** title of the product */
	@Constraints.Required
	private String title;

	/** description of the product */
	private String description;

	/** date the product was added to the user's inventory */
	@Constraints.Required
	@Formats.DateTime(pattern = "dd/MM/yyyy")
	private Date addedSince;

	/** indicates if the product can be bought in the webshop */
	@Constraints.Required
	private boolean canBuy;

	/** indicates the price of the product if it can be bought */
	private float buyPrice;

	/** indicates if the product can be traded in the webshop */
	@Constraints.Required
	private boolean canTrade;

	/** last know mail-address (used for refunding to seller / can't change mail for refund) */
	@Constraints.Required
	private String mailLast;

	/** mail-address used for logging in currently */
	@Constraints.Required
	private String mailCurrent;

	/** password used for logging in currently */
	@Constraints.Required
	private String passwordCurrent;

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

	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getAddedSince() {
		return addedSince;
	}

	public void setAddedSince(Date addedSince) {
		this.addedSince = addedSince;
	}

	public boolean isCanBuy() {
		return canBuy;
	}

	public void setCanBuy(boolean canBuy) {
		this.canBuy = canBuy;
	}

	public float getBuyPrice() {
		return buyPrice;
	}

	public void setBuyPrice(float buyPrice) {
		this.buyPrice = buyPrice;
	}

	public boolean isCanTrade() {
		return canTrade;
	}

	public void setCanTrade(boolean canTrade) {
		this.canTrade = canTrade;
	}

	public String getMailLast() {
		return mailLast;
	}

	public void setMailLast(String mailLast) {
		this.mailLast = mailLast;
	}

	public String getMailCurrent() {
		return mailCurrent;
	}

	public void setMailCurrent(String mailCurrent) {
		this.mailCurrent = mailCurrent;
	}

	public String getPasswordCurrent() {
		return passwordCurrent;
	}

	public void setPasswordCurrent(String passwordCurrent) {
		this.passwordCurrent = passwordCurrent;
	}
}