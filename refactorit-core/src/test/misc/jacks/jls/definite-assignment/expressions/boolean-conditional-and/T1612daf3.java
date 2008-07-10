
public class T1612daf3 {
    public static void main(String[] args) {
        
        boolean x, y = true;
        if (true && y); // DA after a, but not after b
        else y = x;
    
    }
}
