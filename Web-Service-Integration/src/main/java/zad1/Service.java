/**
 *
 * @author Percheklii Andrii s33232
 *
 */

package zad1;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Currency;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Service {
    private static final String OPENWEATHER_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String FIXER_URL = "http://data.fixer.io/api/latest";
    private static final String NBP_TABLE_A_URL = "https://www.nbp.pl/kursy/kursya.html";
    private static final String NBP_TABLE_B_URL = "https://www.nbp.pl/kursy/kursyb.html";

    private static final String API_KEY_OPENWEATHER = "YOUR_API_KEY_HERE";
    private static final String API_KEY_FIXER = "YOUR_API_KEY_HERE";

    private final String countryIso2;
    private final String countryCurrencyCode;
    private final HttpClient http;

    public Service(String kraj) {
        this.countryIso2 = resolveCountryIso2(kraj)
                .orElseThrow(() -> new IllegalArgumentException("Nieznany kraj: " + kraj));
        this.countryCurrencyCode = resolveCurrencyCode(this.countryIso2)
                .orElseThrow(() -> new IllegalArgumentException("Cannot determine currency for country: " + kraj));
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public String getWeather(String miasto) {
        String q = miasto + "," + countryIso2;
        String url = OPENWEATHER_URL
                + "?q=" + urlEncode(q)
                + "&appid=" + API_KEY_OPENWEATHER
                + "&units=metric"
                + "&lang=pl";

        return httpGet(url);
    }

    public Double getRateFor(String kod_waluty) {
        String target = kod_waluty == null ? "" : kod_waluty.trim().toUpperCase(Locale.ROOT);
        if (target.isEmpty()) throw new IllegalArgumentException("Pusty kod waluty.");

        String base = countryCurrencyCode;
        if (base.equalsIgnoreCase(target)) return 1.0;

        String url = FIXER_URL
                + "?access_key=" + API_KEY_FIXER
                + "&symbols=" + base + "," + target;

        String json = httpGet(url);

        Double basePerEur = base.equals("EUR") ? 1.0 : extractJsonNumber(json, "\"" + Pattern.quote(base) + "\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)");
        Double targetPerEur = target.equals("EUR") ? 1.0 : extractJsonNumber(json, "\"" + Pattern.quote(target) + "\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)");

        if (basePerEur == null || targetPerEur == null) {
            throw new IllegalStateException("Failed to read rates from Fixer. Server response: " + json);
        }
        if (basePerEur == 0.0) throw new IllegalStateException("Invalid base rate from Fixer.");

        return targetPerEur / basePerEur;
    }

    public Double getNBPRate() {
        String code = countryCurrencyCode;
        if ("PLN".equalsIgnoreCase(code)) return 1.0;

        String htmlA = httpGet(NBP_TABLE_A_URL);
        Double rate = extractNbpMidRateFromHtml(htmlA, code);
        if (rate != null) return rate;

        String htmlB = httpGet(NBP_TABLE_B_URL);
        rate = extractNbpMidRateFromHtml(htmlB, code);
        if (rate != null) return rate;

        throw new IllegalStateException("Nie znaleziono waluty " + code + " w tabelach NBP A i B.");
    }


    public String getCountryCurrencyCode() {
        return countryCurrencyCode;
    }

    private String httpGet(String url) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(20))
                    .GET()
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (resp.statusCode() / 100 != 2) {
                throw new IllegalStateException("HTTP " + resp.statusCode() + " dla: " + url + "\n" + resp.body());
            }
            return resp.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Przerwano pobieranie: " + url, e);
        } catch (IOException e) {
            throw new IllegalStateException("Download error: " + url, e);
        }
    }

    private static Optional<String> resolveCountryIso2(String countryName) {
        if (countryName == null || countryName.trim().isEmpty()) return Optional.empty();
        String name = countryName.trim();

        for (String iso2 : Locale.getISOCountries()) {
            Locale l = new Locale.Builder().setRegion(iso2).build();
            if (l.getDisplayCountry(Locale.ENGLISH).equalsIgnoreCase(name) ||
                l.getDisplayCountry(Locale.forLanguageTag("pl-PL")).equalsIgnoreCase(name)) {
                return Optional.of(iso2);
            }
        }
        return Optional.empty();
    }

    private static Optional<String> resolveCurrencyCode(String countryIso2) {
        try {
            Currency c = Currency.getInstance(new Locale.Builder().setRegion(countryIso2).build());
            return Optional.ofNullable(c == null ? null : c.getCurrencyCode());
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private static String urlEncode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static Double extractJsonNumber(String json, String regex) {
        if (json == null) return null;
        Matcher m = Pattern.compile(regex).matcher(json);
        if (!m.find()) return null;
        return Double.valueOf(m.group(1));
    }

    private static Double extractNbpMidRateFromHtml(String html, String currencyCode) {
        if (html == null) return null;
        String code = Pattern.quote(currencyCode.toUpperCase(Locale.ROOT));
        
        Pattern row = Pattern.compile(
                "<td[^>]*>\\s*(\\d+)\\s+(" + code + ")\\s*</td>\\s*<td[^>]*>\\s*([0-9]+,[0-9]+)\\s*</td>",
                Pattern.CASE_INSENSITIVE
        );
        Matcher m = row.matcher(html);
        if (!m.find()) return null;

        int multiplier = Integer.parseInt(m.group(1));
        double plnForMultiplier = Double.parseDouble(m.group(3).replace(',', '.'));
        return multiplier > 0 ? plnForMultiplier / multiplier : null;
    }
}