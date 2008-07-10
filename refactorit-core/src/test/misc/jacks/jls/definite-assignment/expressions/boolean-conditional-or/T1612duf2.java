
public class T1612duf2 {
    public static void main(String[] args) {
        
        final boolean x;
        boolean y = false;
        if ((x = false) || false) // DU after b, but not after a
            x = y;
    
    }
}
