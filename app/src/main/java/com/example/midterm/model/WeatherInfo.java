package com.example.midterm.model;

public class WeatherInfo {
    private String description;
    private double temperature;
    private int humidity;
    private double windSpeed;
    private String icon;
    private String condition;

    public WeatherInfo() {
    }

    public WeatherInfo(String description, double temperature, int humidity, double windSpeed, String icon, String condition) {
        this.description = description;
        this.temperature = temperature;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.icon = icon;
        this.condition = condition;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public boolean isOutdoorFriendly() {
        // Consider weather good for outdoor events if:
        // - Temperature between 18-30°C
        // - Not rainy/stormy
        // - Humidity below 80%
        if (temperature < 18 || temperature > 35) return false;
        if (humidity > 85) return false;
        if (condition != null) {
            String lower = condition.toLowerCase();
            if (lower.contains("rain") || lower.contains("storm") || lower.contains("thunder")) {
                return false;
            }
        }
        return true;
    }
}
