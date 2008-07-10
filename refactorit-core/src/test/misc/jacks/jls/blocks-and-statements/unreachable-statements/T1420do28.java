
public class T1420do28 {
    public static void main(String[] args) {
        
        a: do try {
            continue a;
        } finally {
            return;
        } while (false);
        { int x=3; }
    
    }
}
