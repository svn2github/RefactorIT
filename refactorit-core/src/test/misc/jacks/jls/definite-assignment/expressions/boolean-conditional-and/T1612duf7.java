
public class T1612duf7 {
    public static void main(String[] args) {
        
        final boolean x;
        boolean y = (x = true) && false;
        // DU after a && b when true, but not when false
        x = y;
    
    }
}
