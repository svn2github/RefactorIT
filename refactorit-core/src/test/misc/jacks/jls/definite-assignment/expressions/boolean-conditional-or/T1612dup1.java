
public class T1612dup1 {
    public static void main(String[] args) {
        
        final boolean x;
        if ((x = false) || true);
        else x = false;
    
    }
}
