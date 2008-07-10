
public class T1420do25 {
    public static void main(String[] args) {
        
        a: do try {
            continue a;
        } finally {
            return;
        } while (false);
        int i;
    
    }
}
