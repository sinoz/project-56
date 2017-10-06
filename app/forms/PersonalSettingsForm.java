package forms;

import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.List;

/** A template of the 'edit personal settings' form.
 * @author I.A
 */
public final class PersonalSettingsForm implements Constraints.Validatable<List<ValidationError>> {
	public String usernameToChangeTo;

	@Override
	public List<ValidationError> validate() {
		List<ValidationError> errors = new ArrayList<>();

		if (usernameToChangeTo.length() < 4) {
			errors.add(new ValidationError("usernameToChangeTo", "Username must be longer than 4 characters"));
		}

		return errors;
	}
}
