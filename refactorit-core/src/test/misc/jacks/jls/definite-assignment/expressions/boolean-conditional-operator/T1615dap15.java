
public class T1615dap15 {
    public static void main(String[] args) {
        
	boolean a = true, x;
	if (a ? x = true : (x = true))
	    a = x;
    
    }
}
