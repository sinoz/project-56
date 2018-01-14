package forms;

import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.List;

/**
 * A template of the 'edit password' form.
 *
 * @author Maurice van Veen
 */
@Constraints.Validate
public final class PersonalSettingsPasswordForm implements Constraints.Validatable<List<ValidationError>> {
	@Constraints.Required
	public String currentPassword;

	@Constraints.Required
	public String password;

	@Constraints.Required
	public String repeatPassword;

	@Override
	public List<ValidationError> validate() {
		List<ValidationError> errors = new ArrayList<>();

		if (currentPassword.length() > 64) {
			errors.add(new ValidationError("currentPassword", "Password must be maximum of 64 characters long"));
		}

		if (password.length() < 4) {
			errors.add(new ValidationError("password", "Password must be at least 4 characters long"));
		}

		if (password.length() > 64) {
			errors.add(new ValidationError("password", "Password must be maximum of 64 characters long"));
		}

		if (!password.equals(repeatPassword)) {
			errors.add(new ValidationError("repeatPassword", "Passwords do not match."));
		}

		if (currentPassword.equals(password)) {
            errors.add(new ValidationError("password", "Password can't be the same as the old password"));
        }

        if (currentPassword.equals(repeatPassword)) {
            errors.add(new ValidationError("repeatPassword", "Password can't be the same as the old password"));
        }

		return errors;
	}
}
