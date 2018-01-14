package util;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** @author I.A */
public final class RecaptchaUtils {
	/**
	 * Indicates whether to integrate the Recaptcha into the application.
	 */
	public static boolean INTEGRATE_RECAPTCHA = false;

	/**
	 * The secret ReCaptcha key.
	 */
	private static final String SECRET_KEY =  "6LftmzsUAAAAAFJj-lNvdasZ0g_fueVq2-VeAjIE";

	/**
	 * The public ReCaptcha key to include in the view.
	 */
	public static final String PUBLIC_KEY = "6LftmzsUAAAAAGUwS2TcqvKlM8WpLwSv53KpljhL";

	/**
	 * The required {@link WSClient} to perform a REST request.
	 */
	private final WSClient ws;

	/**
	 * Creates a new {@link RecaptchaUtils}.
	 */
	@Inject
	public RecaptchaUtils(WSClient ws) {
		this.ws = ws;
	}

	public boolean isRecaptchaValid(String response) {
		WSRequest holder = ws.url("https://www.google.com/recaptcha/api/siteverify");
		holder.addQueryParameter("secret", SECRET_KEY);
		holder.addQueryParameter("response", response);

		CompletionStage<JsonNode> jsonPromise = holder.post("").thenApply(WSResponse::asJson);

		JsonNode googleResponse = null;
		try{
			googleResponse = jsonPromise.toCompletableFuture().get(1000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}

		return googleResponse!= null && googleResponse.get("success") != null && googleResponse.get("success").asBoolean();
	}
}
