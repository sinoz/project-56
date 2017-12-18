package forms;

import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Maurice van Veen
 */
@Constraints.Validate
public final class SessionMailForm implements Constraints.Validatable<List<ValidationError>> {

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

        if (!email.contains("@") || !email.contains(".")) {
            errors.add(new ValidationError("email", "It has to be a valid email."));
        }

        return errors;
    }

    public String getMail() {
        return email;
    }
}