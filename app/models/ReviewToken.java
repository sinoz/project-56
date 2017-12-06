package models;

import play.data.validation.Constraints;

import javax.persistence.Id;

/**
 * Review written by a user for a user.
 *
 * @author Maurice van Veen
 */
public final class ReviewToken {

	@Constraints.Required
    private String reviewid;

	/** user for whom the review is meant, review is displayed on this user's account */
	@Constraints.Required
	private int userReceiverId;

	/** user who wrote the review */
	@Constraints.Required
	private int userSenderId;

	private int productid;

	public String getReviewID() {
	    return reviewid;
    }

    public void setReviewID(String reviewid) {
	    this.reviewid = reviewid;
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

	public int getProductId() {
	    return productid;
    }

    public void setProductId(int productid) {
	    this.productid = productid;
    }
}