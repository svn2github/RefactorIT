
public class T1612duf7 {
    public static void main(String[] args) {
        
        final boolean x;
        boolean y = (x = false) || true;
        // DU after a || b when false, but not when true
        x = y;
    
    }
}
