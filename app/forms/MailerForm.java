package forms;

import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        if (email.length() < 5) {
            errors.add(new ValidationError("email", "Email must be at least 5 characters long"));
        }

        if (email.length() > 128) {
            errors.add(new ValidationError("email", "Email must be maximum of 128 characters long"));
        }

        if (!email.contains("@") || !email.contains(".")) {
            errors.add(new ValidationError("email", "It has to be a valid email."));
        }

        if (containsNumbers(phone)) {
            errors.add(new ValidationError("phone", "This is an invalid number"));
        }

        if (name.length() <= 1 || name.equals("name")) {
            errors.add(new ValidationError("name", "Please fill in a real name"));
        }

        return errors;
    }

    public String getName() {
        return name;
    }

    public String getMail() {
        return email;
    }

    public String getContent() {
        return content;
    }

    public String getPhone() {
        return phone;
    }

    private boolean containsNumbers(String text) {
        Pattern pattern = Pattern.compile("[0-9()+ ]*");
        Matcher matcher = pattern.matcher(text);
        return !matcher.matches();
    }
}