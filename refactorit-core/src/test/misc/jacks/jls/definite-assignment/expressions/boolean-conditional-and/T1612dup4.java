
public class T1612dup4 {
    public static void main(String[] args) {
        
        final boolean x;
        x = true;
        boolean y = false && (x = false);
    
    }
}
