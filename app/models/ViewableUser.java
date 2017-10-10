package models;

import java.util.Date;

/**
 * A viewable user.
 *
 * @author Maurice van Veen
 */
public final class ViewableUser {

	/** username displayed in the webshop / can be used for log in */
	private String username;

	/** mail used to log in */
	private String mail;

	/** link to the image as profile picture for the user */
	private String profilePicture;

	/** date of when the user first joined, it is only set once */
	private Date memberSince;

	public ViewableUser(String username, String mail, String profilePicture, Date memberSince) {
		this.username = username;
		this.mail = mail;
		this.profilePicture = profilePicture;
		this.memberSince = memberSince;
	}

	public String getUsername() {
		return username;
	}

	public String getMail() {
		return mail;
	}

	public String getProfilePicture() {
		return profilePicture;
	}

	public Date getMemberSince() {
		return memberSince;
	}
}
