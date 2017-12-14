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
		String recaptchaSecretKey = "6LftmzsUAAAAAFJj-lNvdasZ0g_fueVq2-VeAjIE";

		WSRequest holder = ws.url("https://www.google.com/recaptcha/api/siteverify");
		holder.addQueryParameter("secret", recaptchaSecretKey);
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

		if (googleResponse!= null && googleResponse.get("success") != null && googleResponse.get("success").asBoolean()) {
			return true;
		}

		return false;
	}
}
