
public class T1612daf7 {
    public static void main(String[] args) {
        
        boolean x, y = true;
        y = true && (true ? true : y) // DA after a && b when false,
                                      // but not when true
        y = x;
    
    }
}
