
public class T1612daf2 {
    public static void main(String[] args) {
        
        boolean x, y = true;
        if (y && true); // DA after b, but not after a
        else y = x;
    
    }
}
