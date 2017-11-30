package forms;

import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.List;

/**
 * A blue print of a filled in ReStart review form.
 *
 * @author Joris Stander
 */
@Constraints.Validate
public final class ReviewForm implements Constraints.Validatable<List<ValidationError>> {

    @Constraints.Required
    public String Title;

    @Constraints.Required
    public String description;

    @Constraints.Required
    public int rating;

    @Override
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if (Title.length() > 64) {
            errors.add(new ValidationError("title", "Title is too long. pls make it shorter"));
        }
        if (Title.length() < 2) {
            errors.add(new ValidationError("name", "Please fill in a normal title"));
        }

        if (description.length() < 2) {
            errors.add(new ValidationError("description", "please fill in a normal description"));
        }

        if (description.length() > 128) {
            errors.add(new ValidationError("description", "Description can only be 128 characters"));
        }

        if (rating < 1) {
            errors.add(new ValidationError("rating", "please fill in a rating from 1 to 5 stars"));
        }

        if (rating > 5) {
            errors.add(new ValidationError("rating", "please fill in a rating from 1 to 5 stars"));
        }

        return errors;
    }

    public String getTitle()
    {
        return Title;
    }

    public String getDescription()
    {
        return description;
    }

    public int getRating()
    {
        return rating;
    }
}
