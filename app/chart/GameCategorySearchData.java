package chart;

import models.GameCategory;
import models.ViewableUser;
import services.ProductService;
import services.UserViewService;

import java.util.*;

public final class GameCategorySearchData extends ChartData {

	public GameCategorySearchData(ProductService productService) {
		List<GameCategory> gameCategories = productService.fetchGameCategories();

		labels = new String[gameCategories.size()];
		data = new int[gameCategories.size()];
		for (int i = 0; i < gameCategories.size(); i++) {
		    GameCategory gc = gameCategories.get(i);
		    labels[i] = gc.getName();
		    data[i] = gc.getSearch();
        }
	}
}
