package models;

import java.util.Date;
import java.util.List;

/**
 * A viewable user.
 *
 * @author Maurice van Veen
 * @author Johan van der Hoeven
 */
public final class ViewableUser {
	/** userid for this user */
	private int id;

	/** username displayed in the webshop / can be used for log in */
	private String username;

	/** link to the image as profile picture for the user */
	private String profilePicture;

	/** date of when the user first joined, it is only set once */
	private Date memberSince;

	// TODO:
//	/** inventory from this user */
//	private List<String> inventory;

	public ViewableUser(int id, String username, String profilePicture, Date memberSince) {
		this.id = id;
		this.username = username;
		this.profilePicture = profilePicture;
//		this.inventory = inventory;
		this.memberSince = memberSince;
	}

	public int getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getProfilePicture() {
		return profilePicture;
	}

	public Date getMemberSince() {
		return memberSince;
	}

//	public List<String> getInventory() {
//		return inventory;
//	}
}
