
public class T1612dup2 {
    public static void main(String[] args) {
        
        final boolean x;
        if (true && ((x = false) || true));
        else x = true;
    
    }
}
