
public class T1612daf6 {
    public static void main(String[] args) {
        
        boolean x, y = false;
        y = y || (x = false) // DA after a || b when false, but not when true
        y = x;
    
    }
}
