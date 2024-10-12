package com.backend.hh24.controllers;

import java.util.Arrays;

public class DateTime {
    private String date;
    private String[] conditions;
    private double[] temps;
    private double[] windSpeeds;
    private double[] otherMetric;  // This could be humidity or any other important metric

    public DateTime(String date, String[] conditions, double[] temps, double[] windSpeeds, double[] otherMetric) {
        this.date = date;
        this.conditions = conditions;
        this.temps = temps;
        this.windSpeeds = windSpeeds;
        this.otherMetric = otherMetric;
    }

    public String getDate() {
        return date;
    }

    public String[] getConditions() {
        return conditions;
    }

    public double[] getTemps() {
        return temps;
    }

    public double[] getWindSpeeds() {
        return windSpeeds;
    }

    public double[] getOtherMetric() {
        return otherMetric;
    }

    @Override
    public String toString() {
        return "Date: " + date + "\n" +
                "Conditions: " + Arrays.toString(conditions) + "\n" +
                "Temperatures: " + Arrays.toString(temps) + "\n" +
                "Wind Speeds: " + Arrays.toString(windSpeeds) + "\n" +
                "Other Metric: " + Arrays.toString(otherMetric);
    }
}
