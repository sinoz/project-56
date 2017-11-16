package models;

public class CouponCode {
	private final String code;
	private final double percentage;

	public CouponCode(String code, double percentage) {
		this.code = code;
		this.percentage = percentage;
	}

	public String getCode() {
		return code;
	}

	public double getPercentage() {
		return percentage;
	}
}
