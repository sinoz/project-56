package forms;

import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.List;

/** TODO
  * @author I.A
  */
public final class CouponCodeForm implements Constraints.Validatable<List<ValidationError>> {
	public String coupon;

	@Override
	public List<ValidationError> validate() {
		List<ValidationError> errors = new ArrayList<>();

		// TODO

		return errors;
	}
}
