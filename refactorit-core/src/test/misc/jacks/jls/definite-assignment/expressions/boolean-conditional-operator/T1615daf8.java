
public class T1615daf8 {
    public static void main(String[] args) {
        
	boolean a = false, x;
	if (a ? true : (x = true))
	    a = x;
    
    }
}
