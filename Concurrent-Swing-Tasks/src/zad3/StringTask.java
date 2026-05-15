package zad3;

import java.util.concurrent.Callable;

/**
 * Zadanie do wykonania - podobne do zad1, ale implementuje Callable dla Future
 */
public class StringTask implements Callable<String> {
    private String text;
    private int count;
    
    public StringTask(String text, int count) {
        this.text = text;
        this.count = count;
    }
    
    @Override
    public String call() throws Exception {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < count; i++) {

            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("Zadanie zostało przerwane");
            }
            result.append(text);
            Thread.sleep(10);
        }
        return result.toString();
    }
}

