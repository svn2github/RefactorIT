
public class T1615dap11 {
    public static void main(String[] args) {
        
	boolean a = true, x;
	if (a ? false : (x = true))
	    a = x;
    
    }
}
