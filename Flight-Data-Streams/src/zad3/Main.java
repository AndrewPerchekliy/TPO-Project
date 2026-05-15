/**
 *
 *  @author Percheklii Andrii S33232
 *
 */

package zad3;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
public class Main {
  public static void main(String[] args) {
    Function<Object, Object> flines = filename -> {
        try {
            return Files.readAllLines(Paths.get((String) filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    };
    
    Function<Object, Object> join = lines -> String.join("", (List<String>) lines);
    
    Function<Object, Object> collectInts = text -> {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher((String) text);
        return matcher.results()
                .mapToInt(match -> Integer.parseInt(match.group()))
                .boxed()
                .collect(Collectors.toList());
    };
    
    Function<Object, Object> sum = numbers -> ((List<Integer>) numbers).stream().mapToInt(Integer::intValue).sum();

    String fname = System.getProperty("user.home") + "/LamComFile.txt"; 
    InputConverter<String> fileConv = new InputConverter<>(fname);
    List<String> lines = fileConv.convertBy(flines);
    String text = fileConv.convertBy(flines, join);
    List<Integer> ints = fileConv.convertBy(flines, join, collectInts);
    Integer sumints = fileConv.convertBy(flines, join, collectInts, sum);

    System.out.println(lines);
    System.out.println(text);
    System.out.println(ints);
    System.out.println(sumints);

    List<String> arglist = Arrays.asList(args);
    InputConverter<List<String>> slistConv = new InputConverter<>(arglist);  
    sumints = slistConv.convertBy(join, collectInts, sum);
    System.out.println(sumints);

  }
}
