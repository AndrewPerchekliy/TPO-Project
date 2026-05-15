package zad3;

import java.util.function.Function;

public class InputConverter<T> {
    private final T input;
    
    public InputConverter(T input) {
        this.input = input;
    }
    
    @SafeVarargs
    public final <R> R convertBy(Function<Object, Object>... functions) {
        Object result = input;
        
        for (Function<Object, Object> function : functions) {
            result = function.apply(result);
        }
        
        @SuppressWarnings("unchecked")
        R typedResult = (R) result;
        return typedResult;
    }
}
