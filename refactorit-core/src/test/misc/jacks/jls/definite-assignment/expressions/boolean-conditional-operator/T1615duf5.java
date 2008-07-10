
public class T1615duf5 {
    public static void main(String[] args) {
        
	final boolean x;
	boolean a = x = true;
	if (a ? true : (x = true));
    
    }
}
