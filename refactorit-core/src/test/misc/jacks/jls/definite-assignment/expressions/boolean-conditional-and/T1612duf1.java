
public class T1612duf1 {
    public static void main(String[] args) {
        
        final boolean x;
        boolean y = x = true;
        if (y && true)
            x = y;
    
    }
}
