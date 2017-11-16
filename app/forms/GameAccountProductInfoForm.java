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
public final class GameAccountProductInfoForm implements Constraints.Validatable<List<ValidationError>> {

	@Constraints.Required
	public String description;

	@Override
	public List<ValidationError> validate() {
		List<ValidationError> errors = new ArrayList<>();

		if (description != null) {
			errors.add(new ValidationError("description", "Description isn't valid."));
		}

		return errors;
	}

	public String getDescription() {
		return description;
	}
}
