
public class T15951e4 {
    public static void main(String[] args) {
        
        try {
            new Object() {
                {
                    if (true) // initializer must be able to complete normally
                        throw new InterruptedException();
                }
            };
        } catch (InterruptedException e) {
        }
    
    }
}
