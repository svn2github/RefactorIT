
public class T1615na4 {
    public static void main(String[] args) {
        
	final boolean x = Boolean.TRUE.booleanValue(); // non-constant
	if (false ? x = true : false);
    
    }
}
