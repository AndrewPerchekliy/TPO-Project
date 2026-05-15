/**
 *
 *  @author Percheklii Andrii S33232
 *
 */

package zad2;

public class Product {
    private int productId;
    private int weight;
    
    public Product(int productId, int weight) {
        this.productId = productId;
        this.weight = weight;
    }
    
    public int getId_towaru() {
        return productId;
    }
    
    public int getWaga() {
        return weight;
    }
}

