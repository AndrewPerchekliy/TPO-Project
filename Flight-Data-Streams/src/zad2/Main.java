/**
 *
 *  @author Percheklii Andrii S33232
 *
 */

package zad2;


import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

  public static void main(String[] args) {
    List<String> dest = Arrays.asList(
      "bleble bleble 2000",
      "WAW HAV 1200",
      "xxx yyy 789",
      "WAW DPS 2000",
      "WAW HKT 1000"
    );
    double ratePLNvsEUR = 4.30;
    List<String> result = 
    dest.stream()
        .filter(flight -> flight.startsWith("WAW "))
        .map(flight -> {
            String[] parts = flight.split(" ");
            String destination = parts[1];
            double priceEUR = Double.parseDouble(parts[2]);
            double pricePLN = priceEUR * ratePLNvsEUR;
            return "to " + destination + " - price in PLN:\t" + (int)pricePLN;
        })
        .collect(Collectors.toList());

    for (String r : result) System.out.println(r);
  }
}
