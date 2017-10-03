package forms;

import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.List;

/**
 * A blue print of a filled in ReStart account registration form.
 * @author I.A
 */
@Constraints.Validate
public final class RegistrationForm implements Constraints.Validatable<List<ValidationError>> {
	@Constraints.Required
	public String name;

	@Constraints.Email
	@Constraints.Required
	public String email;

	@Constraints.Required
	public String password;

	@Constraints.Required
	public String repeatPassword;

	@Override
	public List<ValidationError> validate() {
		List<ValidationError> errors = new ArrayList<>();

		if (name.length() < 2) {
			errors.add(new ValidationError("name", "Name must be at least 2 characters long"));
		}

		if (email.length() < 8) {
			errors.add(new ValidationError("email", "Email must be at least 8 characters long"));
		}

		if (password.length() < 6) {
			errors.add(new ValidationError("password", "Password must be at least 6 characters long"));
		}

		if (!password.equals(repeatPassword)) {
			errors.add(new ValidationError("", "Passwords do not match."));
		}

		return errors;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}

	public String getRepeatPassword() {
		return repeatPassword;
	}
}
