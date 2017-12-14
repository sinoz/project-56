package chart;

import models.Product;
import models.ViewableUser;
import services.ProductService;

import java.util.*;

public final class ProductAddedData extends ChartData {

	public ProductAddedData(ProductService productService) {
		List<Product> products = productService.fetchProducts();

		List<Date> dates = new ArrayList<>();
		HashMap<Date, Integer> scores = new HashMap<>();
		for (Product product : products) {
			Date d = product.getAddedSince();
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
