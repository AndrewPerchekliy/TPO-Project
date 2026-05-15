/**
 *
 *  @author Percheklii Andrii S33232
 *
 */

package zad2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {

    public static void main(String[] args) {
        BlockingQueue<Product> queue = new LinkedBlockingQueue<>();
        AtomicBoolean readingFinished = new AtomicBoolean(false);
        
        Thread threadA = new Thread(() -> {
            try {
                BufferedReader reader = new BufferedReader(new FileReader("../Products.txt"));
                String line;
                int count = 0;
                
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    
                    String[] parts = line.split("\\s+");
                    if (parts.length >= 2) {
                        int id = Integer.parseInt(parts[0]);
                        int weight = Integer.parseInt(parts[1]);
                        
                        Product towar = new Product(id, weight);
                        queue.put(towar);
                        count++;
                        
                        if (count % 200 == 0) {
                            System.out.println("created " + count + " objects");
                        }
                    }
                }
                
                reader.close();
                readingFinished.set(true);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        
        Thread threadB = new Thread(() -> {
            int totalWeight = 0;
            int count = 0;
            
            while (true) {
                try {
                    Product towar = queue.poll(100, TimeUnit.MILLISECONDS);
                    if (towar == null) {
                        if (readingFinished.get() && queue.isEmpty()) {
                            break;
                        }
                        continue;
                    }
                    
                    totalWeight += towar.getWaga();
                    count++;
                    
                    if (count % 100 == 0) {
                        System.out.println("calculated weight of " + count + " products");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
            
            System.out.println("Sumaryczna weight wszystkich towarów: " + totalWeight);
        });
        
        threadA.start();
        threadB.start();
        
        try {
            threadA.join();
            threadB.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
