package forms;

import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.List;

/**
 * The AdminDeleteUser form.
 *
 * @author Johan van der Hoeven
 * @author Maurice van Veen
 */
@Constraints.Validate
public final class AdminDeleteForm implements Constraints.Validatable<List<ValidationError>> {
    @Constraints.Required
    public String adminPassword;

    @Override
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();
        return errors;
    }

    public String getAdminPassword(){
        return adminPassword;
    }
}
