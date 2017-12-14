package models;

import javax.persistence.Id;
import java.util.Date;

/**
 * @author Maurice van Veen
 */
public final class VisitTime {

	@Id
	private int id;

	/** id of the user that was logged in */
	private int userid;

	private Date time;

	public VisitTime(int id, int userid, Date time) {
		this.id = id;
		this.userid = userid;
		this.time = time;
	}

	public int getId() {
		return id;
	}

	public int getUserId() {
		return userid;
	}

	public Date getTime() {
		return time;
	}
}
