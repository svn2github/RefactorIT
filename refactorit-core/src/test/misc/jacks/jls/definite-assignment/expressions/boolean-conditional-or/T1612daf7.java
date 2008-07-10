
public class T1612daf7 {
    public static void main(String[] args) {
        
        boolean x, y = true;
        y = false || (false ? y : false) // DA after a || b when true,
                                         // but not when false
        y = x;
    
    }
}
