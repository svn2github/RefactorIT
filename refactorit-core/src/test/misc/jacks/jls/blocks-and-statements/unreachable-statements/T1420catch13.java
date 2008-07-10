
public class T1420catch13 {
    public static void main(String[] args) {
        
        try {
            return;
        } catch (RuntimeException e) {
            // this cannot be thrown
        }
    
    }
}
