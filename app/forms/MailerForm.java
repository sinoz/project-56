package forms;

import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.List;

/**
 * A blue print of an email send from the contact us page.
 *
 * @author Joris Stander
 */
@Constraints.Validate
public final class MailerForm implements Constraints.Validatable<List<ValidationError>> {
    @Constraints.Required
    public String name;

    @Constraints.Required
    public String content;

    @Constraints.Email
    @Constraints.Required
    public String email;

    @Constraints.Required
    public String phone;

    @Override
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if (email.length() < 8) {
            errors.add(new ValidationError("email", "Email must be at least 8 characters long"));
        }

        if (phone.contains("[0-9]+") == true) {
            errors.add(new ValidationError("phone", "this is an invalid number"));
        }
        if (email.length() > 128) {
            errors.add(new ValidationError("email", "Email must be maximum of 128 characters long"));
        }

        if (name.length() <= 1) {
            errors.add(new ValidationError("name", "fill in a real name dumbass"));
        }

        if (name == ("rick roll") || name == ("name")) {
            errors.add(new ValidationError("name", "You can't Rick roll me!??!?!"));
        }
        return errors;
    }

    public String getmailName() {
        return name;
    }

    public String getmailEmail() {
        return email;
    }

    public String getContent() {
        return email;
    }

    public String getPhone() {
        return email;
    }

}
