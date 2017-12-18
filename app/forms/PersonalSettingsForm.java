package forms;

import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.List;

/**
 * A template of the 'edit personal settings' form.
 *
 * @author I.A
 */
@Constraints.Validate
public final class PersonalSettingsForm implements Constraints.Validatable<List<ValidationError>> {
	@Constraints.Required
	public String usernameToChangeTo;

	@Constraints.Email
	@Constraints.Required
	public String emailToChangeTo;

	public String paymentMailToChangeTo;

	@Constraints.Required
	public String password;

	@Override
	public List<ValidationError> validate() {
		List<ValidationError> errors = new ArrayList<>();

		if (usernameToChangeTo.length() < 4) {
			errors.add(new ValidationError("usernameToChangeTo", "Username must be longer than 4 characters."));
		}

		if (!emailToChangeTo.contains("@") || !emailToChangeTo.contains(".")) {
			errors.add(new ValidationError("emailToChangeTo", "It has to be a valid email."));
		}

		if (!paymentMailToChangeTo.contains("@") || !paymentMailToChangeTo.contains(".")) {
			errors.add(new ValidationError("paymentMailToChangeTo", "It has to be a valid email."));
		}
		return errors;
	}
}
