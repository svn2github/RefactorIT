
public class T1612dup2 {
    public static void main(String[] args) {
        
        final boolean x;
        if (false || ((x = true) && false))
            x = true;
    
    }
}
