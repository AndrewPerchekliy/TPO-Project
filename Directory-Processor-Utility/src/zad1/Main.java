/**
 *
 *  @author Percheklii Andrii S33232
 *
 */

package zad1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {
  public static void main(String[] args) throws IOException {
    Map<String, java.util.List<String>> groups = Files.lines(Paths.get("unixdict.txt"))
        .collect(Collectors.groupingBy(
            word -> {
              char[] chars = word.toCharArray();
              Arrays.sort(chars);
              return new String(chars);
            }
        ));
    
    long maxCount = groups.values().stream()
        .mapToLong(java.util.List::size)
        .max()
        .orElse(0L);
    
    groups.values().stream()
        .filter(group -> group.size() == maxCount)
        .forEach(group -> System.out.println(String.join(" ", group)));
  }
}
