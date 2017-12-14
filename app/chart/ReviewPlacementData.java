package chart;

import models.Order;
import models.Review;
import services.OrderService;
import services.ReviewService;

import java.util.List;

public final class ReviewPlacementData extends ChartData {

	public ReviewPlacementData(ReviewService reviewService, OrderService orderService) {
		List<Order> orders = orderService.fetchOrders();
        List<Review> reviews = reviewService.fetchReviews();

		labels = new String[]{ "orders", "reviews"};
		data = new int[]{ orders.size(), reviews.size() };
	}
}
