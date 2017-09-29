package controllers;

import play.mvc.Controller;
import services.ServiceConstants;
import services.auth.AuthenticationService;

import javax.inject.Named;

/** A {@link Controller} for logging in users.
 * @author I.A
 */
public final class LoginController extends Controller {
	/**
	 * The {@link AuthenticationService} to use to authenticate users.
	 */
	private final AuthenticationService auth;

	/**
	 * Creates a new {@link LoginController}.
	 */
	public LoginController(@Named(ServiceConstants.DEFAULT_AUTH) AuthenticationService auth) {
		this.auth = auth;
	}

	// TODO
}
