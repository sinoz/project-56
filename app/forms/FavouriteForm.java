package forms;

import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.List;

/**
 * A blue print of a favourite form.
 *
 * @author Johan van der Hoeven
 */
@Constraints.Validate
public final class FavouriteForm implements Constraints.Validatable<List<ValidationError>> {
    public String id;

    @Override
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();
        return errors;
    }

    public String getId() {
        return id;
    }
}
