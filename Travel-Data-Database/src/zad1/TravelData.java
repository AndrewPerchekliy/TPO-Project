package zad1;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.*;
import java.util.*;

public class TravelData {
    private File dataDir;
    private List<Offer> offers;

    public TravelData(File dataDir) {
        this.dataDir = dataDir;
        this.offers = new ArrayList<>();
        loadOffers();
    }

    private void loadOffers() {
        if (!dataDir.exists() || !dataDir.isDirectory()) {
            return;
        }

        File[] files = dataDir.listFiles((dir, name) -> name.endsWith(".txt") || !name.contains("."));
        if (files == null) {
            return;
        }

        for (File file : files) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    
                    String[] parts = line.split("\t");
                    if (parts.length >= 7) {
                        Offer offer = new Offer(
                            parts[0].trim(),  // locale
                            parts[1].trim(),  // country
                            parts[2].trim(),  // departure date
                            parts[3].trim(),  // return date
                            parts[4].trim(),  // place
                            parts[5].trim(),  // price
                            parts[6].trim()   // currency
                        );
                        offers.add(offer);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<String> getOffersDescriptionsList(String loc, String dateFormat) {
        List<String> result = new ArrayList<>();
        Locale targetLocale = parseLocale(loc);
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, targetLocale);
        NumberFormat nf = NumberFormat.getNumberInstance(targetLocale);

        for (Offer offer : offers) {
            StringBuilder sb = new StringBuilder();
            
            // Country name - translate if needed
            String country = translateCountry(offer.getCountry(), offer.getLocale(), targetLocale);
            sb.append(country).append(" ");
            
            // Dates
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date depDate = inputFormat.parse(offer.getDepartureDate());
                Date retDate = inputFormat.parse(offer.getReturnDate());
                sb.append(sdf.format(depDate)).append(" ");
                sb.append(sdf.format(retDate)).append(" ");
            } catch (ParseException e) {
                sb.append(offer.getDepartureDate()).append(" ");
                sb.append(offer.getReturnDate()).append(" ");
            }
            
            // Place - translate
            String place = translatePlace(offer.getPlace(), offer.getLocale(), targetLocale);
            sb.append(place).append(" ");
            
            // Price - parse and format according to locale
            try {
                // Parse price according to source locale
                NumberFormat sourceNf = NumberFormat.getNumberInstance(parseLocale(offer.getLocale()));
                Number priceNum = sourceNf.parse(offer.getPrice());
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
                sb.append(formattedPrice).append(" ");
            } catch (ParseException e) {
                sb.append(offer.getPrice()).append(" ");
            }
            
            // Currency
            sb.append(offer.getCurrency());
            
            result.add(sb.toString());
        }

        return result;
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

    private String translateCountry(String country, String sourceLocale, Locale targetLocale) {
        // Country name translations
        Map<String, Map<String, String>> translations = new HashMap<>();
        
        // Polish to English
        Map<String, String> plToEn = new HashMap<>();
        plToEn.put("Japonia", "Japan");
        plToEn.put("Włochy", "Italy");
        plToEn.put("Stany Zjednoczone Ameryki", "United States");
        translations.put("pl_PL", plToEn);
        
        // English to Polish
        Map<String, String> enToPl = new HashMap<>();
        enToPl.put("Japan", "Japonia");
        enToPl.put("Italy", "Włochy");
        enToPl.put("United States", "Stany Zjednoczone Ameryki");
        translations.put("en_GB", enToPl);
        
        // German translations
        Map<String, String> deToEn = new HashMap<>();
        deToEn.put("Japan", "Japan");
        deToEn.put("Italien", "Italy");
        deToEn.put("Vereinigte Staaten", "United States");
        translations.put("de_DE", deToEn);
        
        Map<String, String> deToPl = new HashMap<>();
        deToPl.put("Japan", "Japonia");
        deToPl.put("Italien", "Włochy");
        deToPl.put("Vereinigte Staaten", "Stany Zjednoczone Ameryki");
        
        String targetLang = targetLocale.getLanguage();
        String sourceLang = parseLocale(sourceLocale).getLanguage();
        
        if (targetLang.equals(sourceLang)) {
            return country;
        }
        
        // Try direct translation
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
        
        // If no translation found, return original
        return country;
    }

    private String translatePlace(String place, String sourceLocale, Locale targetLocale) {
        String sourceLang = parseLocale(sourceLocale).getLanguage();
        String targetLang = targetLocale.getLanguage();
        
        if (sourceLang.equals(targetLang)) {
            return place;
        }
        
        // Place translations
        Map<String, Map<String, String>> placeTranslations = new HashMap<>();
        
        // Polish to English
        Map<String, String> plToEn = new HashMap<>();
        plToEn.put("morze", "sea");
        plToEn.put("jezioro", "lake");
        plToEn.put("góry", "mountains");
        placeTranslations.put("pl", plToEn);
        
        // English to Polish
        Map<String, String> enToPl = new HashMap<>();
        enToPl.put("sea", "morze");
        enToPl.put("lake", "jezioro");
        enToPl.put("mountains", "góry");
        placeTranslations.put("en", enToPl);
        
        // German translations
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

    public List<Offer> getOffers() {
        return new ArrayList<>(offers);
    }

    // Inner class to represent an offer
    public static class Offer {
        private String locale;
        private String country;
        private String departureDate;
        private String returnDate;
        private String place;
        private String price;
        private String currency;

        public Offer(String locale, String country, String departureDate, 
                    String returnDate, String place, String price, String currency) {
            this.locale = locale;
            this.country = country;
            this.departureDate = departureDate;
            this.returnDate = returnDate;
            this.place = place;
            this.price = price;
            this.currency = currency;
        }

        public String getLocale() { return locale; }
        public String getCountry() { return country; }
        public String getDepartureDate() { return departureDate; }
        public String getReturnDate() { return returnDate; }
        public String getPlace() { return place; }
        public String getPrice() { return price; }
        public String getCurrency() { return currency; }
    }
}

