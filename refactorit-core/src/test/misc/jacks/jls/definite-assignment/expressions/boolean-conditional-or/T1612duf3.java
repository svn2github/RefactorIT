
public class T1612duf3 {
    public static void main(String[] args) {
        
        final boolean x, y;
        if (false || (x = true)) // DU after a, but not after b
            x = y;
    
    }
}
