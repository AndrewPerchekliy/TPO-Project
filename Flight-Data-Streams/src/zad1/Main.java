/**
 *
 *  @author Percheklii Andrii S33232
 *
 */

package zad1;


import java.util.*;

public class Main {

  static List<String> getPricesInPLN(List<String> destinations, double xrate) {
    return ListCreator.collectFrom(destinations)
                       .when(
                        s -> s.startsWith("WAW")
                        )
                       .mapEvery( 
                        s -> {
                          String[] parts = s.split(" ");
                          String destination = parts[1];
                          double priceEUR = Double.parseDouble(parts[2]);
                          int pricePLN = (int)(priceEUR * xrate);
                          return "to " + destination + " - price in PLN:\t" + pricePLN;
                        }
                        );
  }

  public static void main(String[] args) {
    List<String> dest = Arrays.asList(
      "bleble bleble 2000",
      "WAW HAV 1200",
      "xxx yyy 789",
      "WAW DPS 2000",
      "WAW HKT 1000"
    );
    double ratePLNvsEUR = 4.30;
    List<String> result = getPricesInPLN(dest, ratePLNvsEUR);
    for (String r : result) System.out.println(r);
  }
}
