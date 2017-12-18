package forms;

import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.List;

/**
 * The AdminModifyUser form.
 *
 * @author Johan van der Hoeven
 */
@Constraints.Validate
public final class AdminModifyUserForm implements Constraints.Validatable<List<ValidationError>> {
    @Constraints.Required
    public String username;

    @Constraints.Email
    @Constraints.Required
    public String mail;

    @Constraints.Email
    @Constraints.Required
    public String paymentMail;

    @Constraints.Required
    public boolean isAdmin;

    @Constraints.Required
    public String adminPassword;

//    @Constraints.Required
//    public String profilePicture;

    @Override
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if (username.length() < 2) {
            errors.add(new ValidationError("username", "Username must be at least 2 characters long"));
        }

        if (username.length() > 16) {
            errors.add(new ValidationError("username", "Username must be maximum of 16 characters long"));
        }

        if (mail.length() < 5) {
            errors.add(new ValidationError("mail", "Email must be at least 5 characters long"));
        }

        if (mail.length() > 128) {
            errors.add(new ValidationError("mail", "Email must be maximum of 128 characters long"));
        }

        if (!mail.contains("@") || !mail.contains(".")) {
            errors.add(new ValidationError("mail", "It has to be a valid email."));
        }

        if (paymentMail.length() < 5) {
            errors.add(new ValidationError("paymentMail", "Email must be at least 5 characters long"));
        }

        if (paymentMail.length() > 128) {
            errors.add(new ValidationError("paymentMail", "Email must be maximum of 128 characters long"));
        }

        if (!paymentMail.contains("@") || !paymentMail.contains(".")) {
            errors.add(new ValidationError("paymentMail", "It has to be a valid email."));
        }

        return errors;
    }

    public String getUsername() {
        return username;
    }

    public String getMail(){
        return mail;
    }

    public String getPaymentMail(){
        return paymentMail;
    }

    public boolean getIsAdmin(){
        return isAdmin;
    }

    public String getAdminPassword(){
        return adminPassword;
    }

//    public String getProfilePicture(){
//        return profilePicture;
//    }
}
