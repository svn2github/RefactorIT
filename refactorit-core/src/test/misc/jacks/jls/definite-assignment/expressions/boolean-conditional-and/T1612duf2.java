
public class T1612duf2 {
    public static void main(String[] args) {
        
        final boolean x;
        boolean y = true;
        if ((x = true) && true); // DU after b, but not after a
        else x = y;
    
    }
}
