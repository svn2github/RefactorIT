
public class T1612duf6 {
    public static void main(String[] args) {
        
        final boolean x;
        boolean y = true || ((x = false) && false);
        // DU after a || b when true, but not when false
        x = y;
    
    }
}
