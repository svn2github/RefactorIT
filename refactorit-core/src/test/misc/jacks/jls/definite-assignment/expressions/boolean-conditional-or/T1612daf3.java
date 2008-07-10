
public class T1612daf3 {
    public static void main(String[] args) {
        
        boolean x, y = false;
        if (false || y) // DA after a, but not after b
            y = x;
    
    }
}
