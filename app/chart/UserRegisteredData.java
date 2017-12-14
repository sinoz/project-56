package chart;

import models.ViewableUser;
import services.UserViewService;

import java.util.*;

public final class UserRegisteredData extends ChartData {

	public UserRegisteredData(UserViewService userViewService) {
		List<ViewableUser> users = userViewService.fetchViewableUsers();

		List<Date> dates = new ArrayList<>();
		HashMap<Date, Integer> scores = new HashMap<>();
		for (ViewableUser user : users) {
		    Date d = user.getMemberSince();
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
