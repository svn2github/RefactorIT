
public class T1420catch2 {
    public static void main(String[] args) {
        
        try {
            int i = 0;
            i /= i;
        } catch (ClassCastException e) {
        }
    
    }
}
