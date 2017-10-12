package models;

import io.ebean.Model;
import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Review written by a user for a user.
 *
 * @author Maurice van Veen
 */
@Entity(name = "review")
public final class Review extends Model {
	@Id
	private String id;

	/** user for whom the review is meant, review is displayed on this user's account */
	@Constraints.Required
	private int userReceiverId;

	/** user who wrote the review */
	@Constraints.Required
	private int userSenderId;

	/** title of the review */
	@Constraints.Required
	private String title;

	/** description of the review */
	private String description;

	/** user rating (0-5) */
	@Constraints.Required
	private int rating;

	private User sender;
	private User receiver;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getUserReceiverId() {
		return userReceiverId;
	}

	public void setUserReceiverId(int userReceiverId) {
		this.userReceiverId = userReceiverId;
	}

	public int getUserSenderId() {
		return userSenderId;
	}

	public void setUserSenderId(int userSenderId) {
		this.userSenderId = userSenderId;
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

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	public User getSender() {
		return sender;
	}

	public void setSender(User sender) {
		this.sender = sender;
	}

	public User getReceiver() {
		return receiver;
	}

	public void setReceiver(User receiver) {
		this.receiver = receiver;
	}
}