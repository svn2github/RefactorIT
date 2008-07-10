
public class T1615na3 {
    public static void main(String[] args) {
        
	final boolean x = Boolean.TRUE.booleanValue(); // non-constant
	if (true ? true : (x = true));
    
    }
}
