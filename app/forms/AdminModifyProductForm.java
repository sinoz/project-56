package forms;

import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.List;

/**
 * The AdminModifyProduct form.
 *
 * @author Maurice van Veen
 */
@Constraints.Validate
public final class AdminModifyProductForm implements Constraints.Validatable<List<ValidationError>> {

    @Constraints.Required
    public int userid;

    @Constraints.Required
    public int gameid;

    @Constraints.Required
    public boolean visible;

    @Constraints.Required
    public boolean disabled;

    @Constraints.MaxLength(32)
    @Constraints.Required
    public String title;

    @Constraints.Required
    public String description;

    @Constraints.Required
    public boolean canbuy;

    @Constraints.Required
    public boolean cantrade;

    @Constraints.Required
    public double buyprice;

    @Constraints.Required
    @Constraints.Email
    public String maillast;

    @Constraints.Required
    @Constraints.Email
    public String mailcurrent;

    @Constraints.Required
    public String currentpassword;

    @Constraints.Required
    public String adminPassword;

    @Override
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if(title.length() < 6 && title.length() > 0){
            errors.add(new ValidationError("title", "The title must be at least 6 characters long"));
        }

        if(!canbuy && !cantrade){
            errors.add(new ValidationError("canBuy", "You must at least select \"For Sale\" or \"Tradeable\""));
            errors.add(new ValidationError("canTrade", "You must at least select \"For Sale\" or \"Tradeable\""));
        }

        if (maillast.length() < 5) {
            errors.add(new ValidationError("maillast", "Email must be at least 5 characters long"));
        }

        if (maillast.length() > 128) {
            errors.add(new ValidationError("maillast", "Email must be maximum of 128 characters long"));
        }

        if (!maillast.contains("@") || !maillast.contains(".")) {
            errors.add(new ValidationError("maillast", "It has to be a valid email."));
        }

        if (mailcurrent.length() < 5) {
            errors.add(new ValidationError("mailcurrent", "Email must be at least 5 characters long"));
        }

        if (mailcurrent.length() > 128) {
            errors.add(new ValidationError("mailcurrent", "Email must be maximum of 128 characters long"));
        }

        if (!mailcurrent.contains("@") || !mailcurrent.contains(".")) {
            errors.add(new ValidationError("mailcurrent", "It has to be a valid email."));
        }

        return errors;
    }

    public String getAdminPassword() {
        return adminPassword;
    }
}