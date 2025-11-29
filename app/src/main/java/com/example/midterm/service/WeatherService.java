package com.example.midterm.service;

import android.os.Handler;
import android.os.Looper;

import com.example.midterm.model.WeatherInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WeatherService {

    // Replace with your OpenWeatherMap API key
    private static final String API_KEY = "YOUR_API_KEY_HERE";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface WeatherCallback {
        void onSuccess(WeatherInfo weatherInfo);
        void onError(String error);
    }

    public void getWeatherForLocation(String city, WeatherCallback callback) {
        executor.execute(() -> {
            try {
                // If API key is not set, return demo data
                if (API_KEY.equals("YOUR_API_KEY_HERE")) {
                    WeatherInfo demoWeather = getDemoWeather(city);
                    mainHandler.post(() -> callback.onSuccess(demoWeather));
                    return;
                }

                String encodedCity = URLEncoder.encode(city, "UTF-8");
                String urlString = BASE_URL + "?q=" + encodedCity + "&appid=" + API_KEY + "&units=metric&lang=vi";

                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    WeatherInfo weatherInfo = parseWeatherResponse(response.toString());
                    mainHandler.post(() -> callback.onSuccess(weatherInfo));
                } else {
                    mainHandler.post(() -> callback.onError("Không thể lấy dữ liệu thời tiết"));
                }

                connection.disconnect();
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi: " + e.getMessage()));
            }
        });
    }

    private WeatherInfo parseWeatherResponse(String jsonString) {
        try {
            JSONObject json = new JSONObject(jsonString);

            JSONArray weatherArray = json.getJSONArray("weather");
            JSONObject weather = weatherArray.getJSONObject(0);
            String description = weather.getString("description");
            String icon = weather.getString("icon");
            String condition = weather.getString("main");

            JSONObject main = json.getJSONObject("main");
            double temperature = main.getDouble("temp");
            int humidity = main.getInt("humidity");

            JSONObject wind = json.getJSONObject("wind");
            double windSpeed = wind.getDouble("speed");

            return new WeatherInfo(description, temperature, humidity, windSpeed, icon, condition);
        } catch (Exception e) {
            return getDemoWeather("Unknown");
        }
    }

    private WeatherInfo getDemoWeather(String city) {
        // Return realistic demo data based on Vietnam's typical weather
        // In production, this would be replaced with real API data

        String[] conditions = {"Clear", "Clouds", "Rain"};
        String[] descriptions = {"Trời quang", "Có mây", "Mưa nhỏ"};
        String[] icons = {"01d", "03d", "10d"};

        // Simple hash to get consistent weather for same city
        int index = Math.abs(city.hashCode()) % 3;

        double baseTemp = 28.0; // Typical Vietnam temperature
        double temp = baseTemp + (index - 1) * 3;
        int humidity = 65 + index * 10;
        double windSpeed = 2.5 + index * 1.5;

        return new WeatherInfo(
            descriptions[index],
            temp,
            humidity,
            windSpeed,
            icons[index],
            conditions[index]
        );
    }
}
