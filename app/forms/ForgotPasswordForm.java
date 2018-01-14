package forms;

import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.List;

/**
 * A blue print of a ReStart forgot password form.
 *
 * @author Maurice van Veen
 */
@Constraints.Validate
public final class ForgotPasswordForm implements Constraints.Validatable<List<ValidationError>> {

	@Constraints.Email
	@Constraints.Required
	public String email;

	@Override
	public List<ValidationError> validate() {
		List<ValidationError> errors = new ArrayList<>();

        if (email.length() < 5) {
            errors.add(new ValidationError("email", "Email must be at least 5 characters long"));
        }

        if (email.length() > 128) {
            errors.add(new ValidationError("email", "Email must be maximum of 128 characters long"));
        }

		return errors;
	}
}
