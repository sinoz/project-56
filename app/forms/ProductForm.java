package forms;

import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.List;

/**
 * A blue print of a addProduct form.
 *
 * @author Johan van der Hoeven
 */
@Constraints.Validate
public final class ProductForm implements Constraints.Validatable<List<ValidationError>> {
    @Constraints.MaxLength(32)
    @Constraints.Required
    public String title;

    @Constraints.Required
    public String description;

    @Constraints.Required
    public boolean canBuy;

    public double buyPrice;

    @Constraints.Required
    public boolean canTrade;

    @Constraints.Email
    @Constraints.MaxLength(128)
    @Constraints.Required
    public String emailCurrent;

    @Constraints.MaxLength(64)
    @Constraints.Required
    public String passwordCurrent;

    public String gameCategory;

    @Override
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if(title.length() < 6 && title.length() > 0){
            errors.add(new ValidationError("title", "The title must be at least 6 characters long"));
        }

        if(!canBuy && !canTrade){
            errors.add(new ValidationError("canBuy", "You must at least select \"For Sale\" or \"Tradeable\""));
            errors.add(new ValidationError("canTrade", "You must at least select \"For Sale\" or \"Tradeable\""));
        }
        return errors;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCanBuy() {
        return canBuy;
    }

    public boolean isCanTrade() {
        return canTrade;
    }

    public String getEmailCurrent() {
        return emailCurrent;
    }

    public String getPasswordCurrent() {
        return passwordCurrent;
    }
}