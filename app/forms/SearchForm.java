package forms;

import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.List;

/**
 * A blue print of a search form.
 *
 * @author Johan van der Hoeven
 */
@Constraints.Validate
public final class SearchForm implements Constraints.Validatable<List<ValidationError>> {
	public String input;

	@Override
	public List<ValidationError> validate() {
		List<ValidationError> errors = new ArrayList<>();
		return errors;
	}

	public String getInput() {
		return input;
	}
}
