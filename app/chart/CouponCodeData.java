package chart;

import models.CouponCode;
import models.Order;
import models.ViewableUser;
import services.CouponCodeService;
import services.OrderService;
import services.UserViewService;

import java.util.*;

public final class CouponCodeData extends ChartData {

	public CouponCodeData(CouponCodeService couponCodeService, OrderService orderService) {
		List<Order> orders = orderService.fetchOrders();
        List<CouponCode> coupons = couponCodeService.fetchCouponCodes();
        List<String> couponCodes = new ArrayList<>();

        for (CouponCode couponCode : coupons)
            couponCodes.add(couponCode.getCode());

		HashMap<String, Integer> scores = new HashMap<>();
		for (String couponCode : couponCodes) {
            scores.put(couponCode, 0);
        }

		for (Order order : orders) {
		    String couponCode = order.getCouponCode();
		    if (couponCode != null && couponCode.length() > 0 && !couponCode.equalsIgnoreCase("null")) {
		        if (scores.containsKey(couponCode)) {
                    scores.put(couponCode, scores.get(couponCode) + 1);
                } else {
                    scores.put(couponCode, 1);
                    couponCodes.add(couponCode);
                }
            }
		}

		labels = new String[couponCodes.size()];
		data = new int[couponCodes.size()];
		for (int i = 0; i < couponCodes.size(); i++) {
		    String c = couponCodes.get(i);
		    labels[i] = c;
		    data[i] = scores.get(c);
        }
	}
}
