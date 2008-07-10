
public class T1615duf2 {
    public static void main(String[] args) {
        
	final boolean x;
	boolean a = x = true;
	if (a ? x = true : false);
    
    }
}
