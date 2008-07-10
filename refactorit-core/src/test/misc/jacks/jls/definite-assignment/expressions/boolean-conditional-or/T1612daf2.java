
public class T1612daf2 {
    public static void main(String[] args) {
        
        boolean x, y = false;
        if (y || false) // DA after b, but not after a
            y = x;
    
    }
}
