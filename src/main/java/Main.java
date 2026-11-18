import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Main {
    public static void main(String[] args) throws Exception {
        String key = System.getenv("YANDEX_WEATHER_KEY");
        if (key == null || key.isBlank()) {
            System.out.println("Set YANDEX_WEATHER_KEY first.");
            return;
        }

        double lat = 55.75;
        double lon = 37.62;
        int limit = 7;
        String url = "https://api.weather.yandex.ru/v2/forecast?lat=" + lat + "&lon=" + lon + "&limit=" + limit;

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
            .header("X-Yandex-Weather-Key", key)
            .GET()
            .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String body = response.body();
        System.out.println(body);

        if (response.statusCode() != 200) {
            return;
        }

        JsonObject root = JsonParser.parseString(body).getAsJsonObject();
        int temp = root.getAsJsonObject("fact").get("temp").getAsInt();
        System.out.println("Current temperature: " + temp);

        JsonArray forecasts = root.getAsJsonArray("forecasts");
        int days = Math.min(limit, forecasts.size());
        if (days == 0) {
            System.out.println("No forecast data.");
            return;
        }
        double sum = 0;
        for (int i = 0; i < days; i++) {
            JsonObject day = forecasts.get(i).getAsJsonObject()
                .getAsJsonObject("parts").getAsJsonObject("day");
            sum += day.get("temp_avg").getAsDouble();
        }
        System.out.println("Average temperature for " + days + " days: " + sum / days);
    }
}
