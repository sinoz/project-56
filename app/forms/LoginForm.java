package forms;

import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.List;

/**
 * A blue print of a ReStart login form.
 *
 * @author Johan van der Hoeven
 */
@Constraints.Validate
public final class LoginForm implements Constraints.Validatable<List<ValidationError>> {
	@Constraints.Required
	public String username;

	@Constraints.Required
	public String password;

	@Override
	public List<ValidationError> validate() {
		List<ValidationError> errors = new ArrayList<>();

		if (username.length() < 2) {
			errors.add(new ValidationError("username", "Username must be at least 2 characters long"));
		}

		if (password.length() < 4) {
			errors.add(new ValidationError("password", "Password must be at least 4 characters long"));
		}

		return errors;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
}
