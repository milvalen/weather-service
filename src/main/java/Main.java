import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class Main {
    private static final double LAT = 55.75;
    private static final double LON = 37.62;
    private static final int LIMIT = 7;

    public static void main(String[] args) {
        try {
            var key = System.getenv("YANDEX_WEATHER_KEY");
            if (key == null || key.isBlank()) {
                throw new RuntimeException("YANDEX_WEATHER_KEY is not set");
            }
            var response = getForecast(key);
            if (response == null) {
                return;
            }
            var body = response.body();
            System.out.println(body);
            if (response.statusCode() != 200) {
                throw new RuntimeException("HTTP %d".formatted(response.statusCode()));
            }
            var root = JsonParser.parseString(body).getAsJsonObject();
            var fact = root.getAsJsonObject("fact");
            var temperature = fact.get("temp").getAsInt();
            System.out.println("Current temperature: %d".formatted(temperature));
            var forecasts = root.getAsJsonArray("forecasts");
            var days = Math.min(LIMIT, forecasts.size());
            if (days == 0) {
                System.out.println("No forecast data.");
                return;
            }

            System.out.println("Average temperature for %d days: %d".formatted(
                days, (int)getAvg(forecasts, days)));
        } catch (Exception e) {
            System.out.println(getError(e));
        }
    }

    static HttpResponse<String> getForecast(String key) {
        try {
            var url = "https://api.weather.yandex.ru/v2/forecast?lat=%s&lon=%s&limit=%d".formatted(LAT, LON, LIMIT);
            var request = HttpRequest.newBuilder(URI.create(url))
                .header("X-Yandex-Weather-Key", key)
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();
            var client = HttpClient.newHttpClient();
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            System.out.println(getError(e));
            return null;
        }
    }

    static double getAvg(JsonArray forecasts, int days) {
        var sum = 0.0;
        for (var i = 0; i < days; i++) {
            var forecast = forecasts.get(i).getAsJsonObject();
            var day = forecast.getAsJsonObject("parts").getAsJsonObject("day");
            sum += day.get("temp_avg").getAsDouble();
        }

        return sum / days;
    }

    static String getError(Exception e) {
        return "%s: %s".formatted(e.getClass().getCanonicalName(), e.getMessage());
    }
}
