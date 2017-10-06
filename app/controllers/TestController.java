package controllers;

import database.DbAccess;
import play.mvc.Controller;

import javax.inject.Inject;

/** TODO
 * @author I.A
 */
public final class TestController extends Controller {
	/**
	 * TODO
	 */
	private DbAccess db;

	/**
	 * TODO
	 */
	@Inject
	public TestController(DbAccess db) {
		this.db = db;
	}

//	public Result index() {
//		return db.select("SELECT * FROM users WHERE username=? AND password=?")
//						 .parameter("mail");
//	}
}
