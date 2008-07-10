
public class T1612duf6 {
    public static void main(String[] args) {
        
        final boolean x;
        boolean y = false && ((x = true) || true);
        // DU after a && b when false, but not when true
        x = y;
    
    }
}
