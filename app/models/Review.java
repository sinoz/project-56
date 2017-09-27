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
	private String userReceiverId;

	/** user who wrote the review */
	@Constraints.Required
	private String userSenderId;

	/** title of the review */
	@Constraints.Required
	private String title;

	/** description of the review */
	private String description;

	/** user rating (0-5) */
	@Constraints.Required
	private int rating;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserReceiverId() {
		return userReceiverId;
	}

	public void setUserReceiverId(String userReceiverId) {
		this.userReceiverId = userReceiverId;
	}

	public String getUserSenderId() {
		return userSenderId;
	}

	public void setUserSenderId(String userSenderId) {
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
}