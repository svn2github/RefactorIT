
public class T1615dap17 {
    public static void main(String[] args) {
        
	boolean a = true, x;
	if (false ? x = true : (x = true))
	    a = x;
    
    }
}
