package forms;

import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Maurice van Veen
 */
@Constraints.Validate
public final class GameCategoryForm implements Constraints.Validatable<List<ValidationError>> {

	@Constraints.Required
	public String name;

	@Constraints.Required
	public String description;

	@Override
	public List<ValidationError> validate() {
		List<ValidationError> errors = new ArrayList<>();

		if (name.length() < 2) {
			errors.add(new ValidationError("name", "Name must be at least 2 characters long"));
		}

		if (name.length() > 32) {
			errors.add(new ValidationError("name", "Name must be maximum of 32 characters long"));
		}

		if (description.length() < 8) {
			errors.add(new ValidationError("description", "Description must be at least 8 characters long"));
		}

		return errors;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}
}