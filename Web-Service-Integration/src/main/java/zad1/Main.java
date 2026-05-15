/**
 *
 * @author Percheklii Andrii s33232
 *
 */

package zad1;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.*;
import java.awt.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        Service s = new Service("Poland");
        String weatherJson = s.getWeather("Warsaw");
        Double rate1 = s.getRateFor("USD");
        Double rate2 = s.getNBPRate();
        
        System.out.println("TEST KONSOLOWY:");
        System.out.println("Pogoda JSON pobrana poprawnie.");
        System.out.println("Kurs PLN wobec USD: " + rate1);
        System.out.println("Kurs NBP PLN: " + rate2);

        SwingUtilities.invokeLater(Main::createAndShowGui);
    }

    private static void createAndShowGui() {
        JFrame frame = new JFrame("Web Service Clients");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(1100, 700);

        JTextField countryField = new JTextField("Poland", 15);
        JTextField cityField = new JTextField("Warsaw", 15);
        JTextField currencyField = new JTextField("USD", 5);
        JButton fetchBtn = new JButton("Pobierz Dane");

        JLabel infoLabel = new JLabel("Wpisz dane i kliknij Pobierz");

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Kraj:")); topPanel.add(countryField);
        topPanel.add(new JLabel("Miasto:")); topPanel.add(cityField);
        topPanel.add(new JLabel("Waluta docelowa:")); topPanel.add(currencyField);
        topPanel.add(fetchBtn);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.add(infoLabel);
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(topPanel, BorderLayout.NORTH);
        headerPanel.add(infoPanel, BorderLayout.SOUTH);

        JPanel weatherUiPanel = new JPanel();
        weatherUiPanel.setLayout(new BoxLayout(weatherUiPanel, BoxLayout.Y_AXIS));
        weatherUiPanel.setBackground(Color.WHITE);
        weatherUiPanel.setBorder(BorderFactory.createEmptyBorder(30, 20, 20, 20));

        JLabel wCity = new JLabel("Brak danych");
        wCity.setFont(new Font("SansSerif", Font.BOLD, 28));
        wCity.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel wDesc = new JLabel("-");
        wDesc.setFont(new Font("SansSerif", Font.ITALIC, 16));
        wDesc.setForeground(Color.GRAY);
        wDesc.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel wTemp = new JLabel("- °C");
        wTemp.setFont(new Font("SansSerif", Font.BOLD, 50));
        wTemp.setForeground(new Color(230, 80, 0)); // Теплый оранжевый цвет
        wTemp.setAlignmentX(Component.CENTER_ALIGNMENT);
        wTemp.setBorder(BorderFactory.createEmptyBorder(15, 0, 25, 0));

        JPanel detailsPanel = new JPanel(new GridLayout(4, 1, 5, 8));
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setMaximumSize(new Dimension(200, 150));
        
        JLabel wFeelsLike = new JLabel("Odczuwalna: -");
        wFeelsLike.setFont(new Font("SansSerif", Font.PLAIN, 15));
        JLabel wPressure = new JLabel("Pressure: -");
        wPressure.setFont(new Font("SansSerif", Font.PLAIN, 15));
        JLabel wHumidity = new JLabel("Humidity: -");
        wHumidity.setFont(new Font("SansSerif", Font.PLAIN, 15));
        JLabel wWind = new JLabel("Wiatr: -");
        wWind.setFont(new Font("SansSerif", Font.PLAIN, 15));

        detailsPanel.add(wFeelsLike);
        detailsPanel.add(wPressure);
        detailsPanel.add(wHumidity);
        detailsPanel.add(wWind);

        weatherUiPanel.add(wCity);
        weatherUiPanel.add(wDesc);
        weatherUiPanel.add(wTemp);
        weatherUiPanel.add(detailsPanel);
        
        JScrollPane weatherScroll = new JScrollPane(weatherUiPanel);
        weatherScroll.setBorder(BorderFactory.createTitledBorder("Aktualna Pogoda"));
        weatherScroll.getViewport().setBackground(Color.WHITE);

        JFXPanel browserPanel = new JFXPanel(); 
        browserPanel.setBorder(BorderFactory.createTitledBorder("Strona Wiki z opisem miasta"));

        AtomicReference<WebEngine> engineRef = new AtomicReference<>();
        Platform.runLater(() -> {
            WebView webView = new WebView();
            engineRef.set(webView.getEngine());
            browserPanel.setScene(new Scene(webView));
        });

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, weatherScroll, browserPanel);
        splitPane.setResizeWeight(0.35);

        frame.setLayout(new BorderLayout());
        frame.add(headerPanel, BorderLayout.NORTH);
        frame.add(splitPane, BorderLayout.CENTER);

        fetchBtn.addActionListener(e -> {
            String country = countryField.getText().trim();
            String city = cityField.getText().trim();
            String targetCurrency = currencyField.getText().trim().toUpperCase();

            fetchBtn.setEnabled(false);
            infoLabel.setText("Pobieranie danych...");

            new SwingWorker<Void, Void>() {
                String weatherResult;
                Double rateFor;
                Double nbpRate;
                String countryCurrency;
                String errorMsg;

                @Override
                protected Void doInBackground() {
                    try {
                        Service service = new Service(country);
                        countryCurrency = service.getCountryCurrencyCode();
                        weatherResult = service.getWeather(city);
                        rateFor = service.getRateFor(targetCurrency);
                        nbpRate = service.getNBPRate();
                    } catch (Exception ex) {
                        errorMsg = ex.getMessage();
                    }
                    return null;
                }

                @Override
                protected void done() {
                    fetchBtn.setEnabled(true);
                    if (errorMsg != null) {
                        infoLabel.setText("An error occurred!");
                        wCity.setText("Error");
                        wDesc.setText(errorMsg);
                        wTemp.setText("-");
                    } else {
                        String info = String.format("Waluta kraju: %s | Kurs %s wobec %s: %.4f | Kurs NBP (PLN za 1 %s): %.4f", 
                                countryCurrency, countryCurrency, targetCurrency, rateFor, countryCurrency, nbpRate);
                        infoLabel.setText(info);
                        
                        String cName = extractValue(weatherResult, "\"name\"\\s*:\\s*\"([^\"]+)\"");
                        String cDesc = extractValue(weatherResult, "\"description\"\\s*:\\s*\"([^\"]+)\"");
                        String cTemp = extractValue(weatherResult, "\"temp\"\\s*:\\s*([0-9\\.\\-]+)");
                        String cFeels = extractValue(weatherResult, "\"feels_like\"\\s*:\\s*([0-9\\.\\-]+)");
                        String cPress = extractValue(weatherResult, "\"pressure\"\\s*:\\s*([0-9]+)");
                        String cHum = extractValue(weatherResult, "\"humidity\"\\s*:\\s*([0-9]+)");
                        String cWind = extractValue(weatherResult, "\"speed\"\\s*:\\s*([0-9\\.\\-]+)");

                        wCity.setText(cName.equals("-") ? city : cName);
                        if (!cDesc.equals("-")) {
                            wDesc.setText(cDesc.substring(0, 1).toUpperCase() + cDesc.substring(1));
                        } else {
                            wDesc.setText("");
                        }
                        wTemp.setText(cTemp + " °C");
                        wFeelsLike.setText("Odczuwalna: " + cFeels + " °C");
                        wPressure.setText("Pressure: " + cPress + " hPa");
                        wHumidity.setText("Humidity: " + cHum + " %");
                        wWind.setText("Wiatr: " + cWind + " m/s");

                        String wikiUrl = "https://pl.wikipedia.org/wiki/" + URLEncoder.encode(city, StandardCharsets.UTF_8);
                        Platform.runLater(() -> {
                            WebEngine engine = engineRef.get();
                            if (engine != null) engine.load(wikiUrl);
                        });
                    }
                }
            }.execute();
        });

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static String extractValue(String json, String regex) {
        if (json == null) return "-";
        Matcher m = Pattern.compile(regex).matcher(json);
        if (m.find()) return m.group(1);
        return "-";
    }
}