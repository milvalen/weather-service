import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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
            System.out.println("Current temperature: %d".formatted(root.getAsJsonObject("fact").get("temp").getAsInt()));
            var forecasts = root.getAsJsonArray("forecasts");
            var days = Math.min(LIMIT, forecasts.size());
            if (days == 0) {
                System.out.println("No forecast data.");
                return;
            }

            System.out.println("Average temperature for %d days: %s".formatted(days, getAvg(forecasts, days)));
        } catch (Exception e) {
            System.out.println(getError(e));
        }
    }

    static HttpResponse<String> getForecast(String key) {
        try {
            return HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create("https://api.weather.yandex.ru/v2/forecast?lat=%s&lon=%s&limit=%d".formatted(LAT, LON, LIMIT)))
                    .header("X-Yandex-Weather-Key", key)
                    .GET()
                    .build(),
                HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            System.out.println(getError(e));
            return null;
        }
    }

    static double getAvg(JsonArray forecasts, int days) {
        var sum = 0.0;
        for (var i = 0; i < days; i++) {
            sum += forecasts.get(i).getAsJsonObject()
                .getAsJsonObject("parts").getAsJsonObject("day").get("temp_avg").getAsDouble();
        }

        return sum / days;
    }

    static String getError(Exception e) {
        return "%s: %s".formatted(e.getClass().getCanonicalName(), e.getMessage());
    }
}
