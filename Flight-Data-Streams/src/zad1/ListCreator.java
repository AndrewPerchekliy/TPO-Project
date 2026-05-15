package zad1;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class ListCreator<T> {
    private List<T> sourceList;
    
    private ListCreator(List<T> sourceList) {
        this.sourceList = sourceList;
    }
    
    public static <T> ListCreator<T> collectFrom(List<T> list) {
        return new ListCreator<>(list);
    }
    
    public ListCreator<T> when(Predicate<T> predicate) {
        List<T> filteredList = new ArrayList<>();
        for (T item : sourceList) {
            if (predicate.test(item)) {
                filteredList.add(item);
            }
        }
        return new ListCreator<>(filteredList);
    }
    
    public <R> List<R> mapEvery(Function<T, R> mapper) {
        List<R> result = new ArrayList<>();
        for (T item : sourceList) {
            result.add(mapper.apply(item));
        }
        return result;
    }
}
