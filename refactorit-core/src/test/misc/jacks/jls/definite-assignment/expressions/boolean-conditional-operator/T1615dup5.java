
public class T1615dup5 {
    public static void main(String[] args) {
        
	final boolean x;
	boolean a = true;
	if (a ? true : (x = true));
    
    }
}
