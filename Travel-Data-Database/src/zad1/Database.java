package zad1;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.Date;

public class Database {
    private String url;
    private TravelData travelData;
    private Connection connection;

    public Database(String url, TravelData travelData) {
        this.url = url;
        this.travelData = travelData;
    }

    public void create() {
        try {
            connection = DriverManager.getConnection(url);
            createTable();
            insertOffers();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create database", e);
        }
    }

    private void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS offers (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "locale TEXT, " +
                    "country TEXT, " +
                    "departure_date TEXT, " +
                    "return_date TEXT, " +
                    "place TEXT, " +
                    "price TEXT, " +
                    "currency TEXT)";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    private void insertOffers() throws SQLException {
        // Clear existing data
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM offers");
        }

        String sql = "INSERT INTO offers (locale, country, departure_date, return_date, place, price, currency) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (TravelData.Offer offer : travelData.getOffers()) {
                pstmt.setString(1, offer.getLocale());
                pstmt.setString(2, offer.getCountry());
                pstmt.setString(3, offer.getDepartureDate());
                pstmt.setString(4, offer.getReturnDate());
                pstmt.setString(5, offer.getPlace());
                pstmt.setString(6, offer.getPrice());
                pstmt.setString(7, offer.getCurrency());
                pstmt.executeUpdate();
            }
        }
    }

    public void showGui() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Travel Offers Database");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 600);

            // Create panel for locale selection
            JPanel controlPanel = new JPanel(new FlowLayout());
            
            JLabel localeLabel = new JLabel("Locale:");
            String[] locales = {"pl_PL", "en_GB", "de_DE"};
            JComboBox<String> localeCombo = new JComboBox<>(locales);
            localeCombo.setSelectedItem("pl_PL");
            
            JLabel dateFormatLabel = new JLabel("Date Format:");
            String[] dateFormats = {"yyyy-MM-dd", "dd.MM.yyyy", "MM/dd/yyyy"};
            JComboBox<String> dateFormatCombo = new JComboBox<>(dateFormats);
            dateFormatCombo.setSelectedItem("yyyy-MM-dd");
            
            controlPanel.add(localeLabel);
            controlPanel.add(localeCombo);
            controlPanel.add(dateFormatLabel);
            controlPanel.add(dateFormatCombo);

            // Create table
            String[] columnNames = {"Country", "Departure Date", "Return Date", "Place", "Price", "Currency"};
            DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
            JTable table = new JTable(tableModel);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            JScrollPane scrollPane = new JScrollPane(table);

            // Update table based on locale selection
            ActionListener updateListener = e -> updateTable(tableModel, 
                    (String) localeCombo.getSelectedItem(), 
                    (String) dateFormatCombo.getSelectedItem());
            
            localeCombo.addActionListener(updateListener);
            dateFormatCombo.addActionListener(updateListener);

            // Initial load
            updateTable(tableModel, "pl_PL", "yyyy-MM-dd");

            frame.add(controlPanel, BorderLayout.NORTH);
            frame.add(scrollPane, BorderLayout.CENTER);
            frame.setVisible(true);
        });
    }

    private void updateTable(DefaultTableModel tableModel, String locale, String dateFormat) {
        tableModel.setRowCount(0);
        
        try {
            String sql = "SELECT * FROM offers";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                Locale targetLocale = parseLocale(locale);
                NumberFormat nf = NumberFormat.getNumberInstance(targetLocale);
                
                while (rs.next()) {
                    String sourceLocale = rs.getString("locale");
                    String country = translateCountry(rs.getString("country"), sourceLocale, targetLocale);
                    
                    String depDate = formatDate(rs.getString("departure_date"), dateFormat, targetLocale);
                    String retDate = formatDate(rs.getString("return_date"), dateFormat, targetLocale);
                    
                    String place = translatePlace(rs.getString("place"), sourceLocale, targetLocale);
                    
                    String price = formatPrice(rs.getString("price"), sourceLocale, targetLocale, nf);
                    String currency = rs.getString("currency");
                    
                    tableModel.addRow(new Object[]{country, depDate, retDate, place, price, currency});
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Locale parseLocale(String localeStr) {
        if (localeStr == null || localeStr.isEmpty()) {
            return Locale.getDefault();
        }
        String[] parts = localeStr.split("_");
        if (parts.length == 2) {
            return Locale.forLanguageTag(parts[0] + "-" + parts[1]);
        } else if (parts.length == 1) {
            return Locale.forLanguageTag(parts[0]);
        }
        return Locale.getDefault();
    }

    private String formatDate(String dateStr, String dateFormat, Locale locale) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = inputFormat.parse(dateStr);
            SimpleDateFormat outputFormat = new SimpleDateFormat(dateFormat, locale);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateStr;
        }
    }

    private String formatPrice(String priceStr, String sourceLocale, Locale targetLocale, NumberFormat nf) {
        try {
            NumberFormat sourceNf = NumberFormat.getNumberInstance(parseLocale(sourceLocale));
            Number priceNum = sourceNf.parse(priceStr);
            // Format according to target locale, removing trailing zeros
            DecimalFormat df = (DecimalFormat) nf;
            df.setMaximumFractionDigits(10);
            df.setMinimumFractionDigits(0);
            df.setGroupingUsed(true);
            String formattedPrice = df.format(priceNum.doubleValue());
            // Remove trailing zeros after decimal point
            if (formattedPrice.contains(".") || formattedPrice.contains(",")) {
                char decimalSep = df.getDecimalFormatSymbols().getDecimalSeparator();
                if (formattedPrice.indexOf(decimalSep) >= 0) {
                    String[] parts = formattedPrice.split("\\" + decimalSep);
                    if (parts.length == 2) {
                        String decimalPart = parts[1].replaceAll("0+$", "");
                        if (decimalPart.isEmpty()) {
                            formattedPrice = parts[0];
                        } else {
                            formattedPrice = parts[0] + decimalSep + decimalPart;
                        }
                    }
                }
            }
            return formattedPrice;
        } catch (ParseException e) {
            return priceStr;
        }
    }

    private String translateCountry(String country, String sourceLocale, Locale targetLocale) {
        Map<String, Map<String, String>> translations = new HashMap<>();
        
        Map<String, String> plToEn = new HashMap<>();
        plToEn.put("Japonia", "Japan");
        plToEn.put("Włochy", "Italy");
        plToEn.put("Stany Zjednoczone Ameryki", "United States");
        translations.put("pl_PL", plToEn);
        
        Map<String, String> enToPl = new HashMap<>();
        enToPl.put("Japan", "Japonia");
        enToPl.put("Italy", "Włochy");
        enToPl.put("United States", "Stany Zjednoczone Ameryki");
        translations.put("en_GB", enToPl);
        
        String targetLang = targetLocale.getLanguage();
        String sourceLang = parseLocale(sourceLocale).getLanguage();
        
        if (targetLang.equals(sourceLang)) {
            return country;
        }
        
        if (sourceLang.equals("pl") && targetLang.equals("en")) {
            Map<String, String> map = translations.get("pl_PL");
            if (map != null && map.containsKey(country)) {
                return map.get(country);
            }
        } else if (sourceLang.equals("en") && targetLang.equals("pl")) {
            Map<String, String> map = translations.get("en_GB");
            if (map != null && map.containsKey(country)) {
                return map.get(country);
            }
        }
        
        return country;
    }

    private String translatePlace(String place, String sourceLocale, Locale targetLocale) {
        String sourceLang = parseLocale(sourceLocale).getLanguage();
        String targetLang = targetLocale.getLanguage();
        
        if (sourceLang.equals(targetLang)) {
            return place;
        }
        
        Map<String, Map<String, String>> placeTranslations = new HashMap<>();
        
        Map<String, String> plToEn = new HashMap<>();
        plToEn.put("morze", "sea");
        plToEn.put("jezioro", "lake");
        plToEn.put("góry", "mountains");
        placeTranslations.put("pl", plToEn);
        
        Map<String, String> enToPl = new HashMap<>();
        enToPl.put("sea", "morze");
        enToPl.put("lake", "jezioro");
        enToPl.put("mountains", "góry");
        placeTranslations.put("en", enToPl);
        
        Map<String, String> deToEn = new HashMap<>();
        deToEn.put("Meer", "sea");
        deToEn.put("See", "lake");
        deToEn.put("Berge", "mountains");
        placeTranslations.put("de", deToEn);
        
        Map<String, String> sourceMap = placeTranslations.get(sourceLang);
        if (sourceMap != null) {
            String english = sourceMap.get(place);
            if (english != null) {
                Map<String, String> targetMap = placeTranslations.get(targetLang);
                if (targetMap != null) {
                    for (Map.Entry<String, String> entry : targetMap.entrySet()) {
                        if (entry.getValue().equals(english)) {
                            return entry.getKey();
                        }
                    }
                }
            }
        }
        
        return place;
    }
}

