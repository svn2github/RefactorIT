
public class T1612duf3 {
    public static void main(String[] args) {
        
        final boolean x, y;
        if (true && (x = false)); // DU after a, but not after b
        else x = y;
    
    }
}
