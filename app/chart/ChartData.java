package chart;

import play.libs.Json;
import play.twirl.api.Html;

public class ChartData {

    protected String[] labels;
    protected int[] data;

    private String[] getLabels() {
        return labels;
    }

    public Html getLabelsHTML() {
        return new Html(Json.toJson(getLabels()).toString());
    }

    private int[] getData() {
        return data;
    }

    public Html getDataHTML() {
        return new Html(Json.toJson(getData()).toString());
    }

    public Html getBackgroundColorHTML() {
        String[] s = new String[data.length];
        for (int i = 0; i < data.length; i++)
            s[i] = "rgba(99, 132, 255, 0.2)";
        return new Html(Json.toJson(s).toString());
    }

    public Html getBorderColorHTML() {
        String[] s = new String[data.length];
        for (int i = 0; i < data.length; i++)
            s[i] = "rgba(99, 132, 255, 1)";
        return new Html(Json.toJson(s).toString());
    }
}