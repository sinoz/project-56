package chart;

import models.VisitTime;
import services.VisitTimeService;

import java.util.*;

public final class WebshopVisitTimesData extends ChartData {

	public WebshopVisitTimesData(VisitTimeService visitTimeService, int userid) {
		List<VisitTime> visittimes = visitTimeService.fetchVisitTimes();

		if (userid != -1) {
			for (int i = visittimes.size() - 1; i >= 0; i--)
				if (visittimes.get(i).getUserId() != userid)
					visittimes.remove(i);
		}

		labels = new String[24];
		for (int i = 0; i < labels.length; i++)
			labels[i] = i + ":00";

		data = new int[labels.length];
		for (int i = 0; i < data.length; i++)
			data[i] = 0;

		for (VisitTime visitTime : visittimes) {
			Date d = visitTime.getTime();
//			GregorianCalendar cal = new GregorianCalendar().;
			System.out.println();
			System.out.println("HI " + d.getHours());
			data[d.getHours()] += 1;
		}
	}
}
