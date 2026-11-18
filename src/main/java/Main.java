import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Main {
    public static void main(String[] args) throws Exception {
        var key = System.getenv("YANDEX_WEATHER_KEY");
        if (key == null || key.isBlank()) {
            System.out.println("Set YANDEX_WEATHER_KEY first.");
            return;
        }

        var lat = 55.75;
        var lon = 37.62;
        var limit = 7;
        var url = "https://api.weather.yandex.ru/v2/forecast?lat=%s&lon=%s&limit=%d".formatted(lat, lon, limit);

        var request = HttpRequest.newBuilder(URI.create(url))
            .header("X-Yandex-Weather-Key", key)
            .GET()
            .build();
        var client = HttpClient.newHttpClient();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        var body = response.body();
        System.out.println(body);

        if (response.statusCode() != 200) {
            return;
        }

        var root = JsonParser.parseString(body).getAsJsonObject();
        var temp = root.getAsJsonObject("fact").get("temp").getAsInt();
        System.out.println("Current temperature: %d".formatted(temp));

        var forecasts = root.getAsJsonArray("forecasts");
        var days = Math.min(limit, forecasts.size());
        if (days == 0) {
            System.out.println("No forecast data.");
            return;
        }
        var sum = 0.0;
        for (var i = 0; i < days; i++) {
            var day = forecasts.get(i).getAsJsonObject()
                .getAsJsonObject("parts").getAsJsonObject("day");
            sum += day.get("temp_avg").getAsDouble();
        }
        System.out.println("Average temperature for %d days: %s".formatted(days, sum / days));
    }
}
