package chart;

import models.Order;
import models.Product;
import services.OrderService;

import java.util.*;

public final class OrderPlacedData extends ChartData {

	public OrderPlacedData(OrderService orderService) {
		List<Order> orders = orderService.fetchOrders();

		List<Date> dates = new ArrayList<>();
		HashMap<Date, Integer> scores = new HashMap<>();
		for (Order order : orders) {
			Date d = order.getOrderplaced();
		    if (!dates.contains(d)) {
                dates.add(d);
                scores.put(d, 1);
            } else {
		        scores.put(d, scores.get(d) + 1);
            }
		}

        Collections.sort(dates);

        // TODO: only show of the last month

		labels = new String[dates.size()];
		data = new int[dates.size()];
		for (int i = 0; i < dates.size(); i++) {
		    Date d = dates.get(i);
		    labels[i] = d.toString();
		    data[i] = scores.get(d);
        }
	}
}
