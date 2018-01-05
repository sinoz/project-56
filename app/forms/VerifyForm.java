package forms;

import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.List;

/**
 * A blue print of a ReStart verify form.
 *
 * @author Maurice van Veen
 */
@Constraints.Validate
public final class VerifyForm implements Constraints.Validatable<List<ValidationError>> {
	@Constraints.Required
	public String verification;

	@Override
	public List<ValidationError> validate() {
		List<ValidationError> errors = new ArrayList<>();
		return errors;
	}
}
