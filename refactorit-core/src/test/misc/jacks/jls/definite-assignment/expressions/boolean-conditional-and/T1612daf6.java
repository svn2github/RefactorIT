
public class T1612daf6 {
    public static void main(String[] args) {
        
        boolean x, y = true;
        y = y && (x = true) // DA after a && b when true, but not when false
        y = x;
    
    }
}
