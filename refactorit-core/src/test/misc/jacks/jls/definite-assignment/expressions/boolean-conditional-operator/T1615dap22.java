
public class T1615dap22 {
    public static void main(String[] args) {
        
	boolean a = true, x;
	if (a ? true : (x = false));
	else a = x;
    
    }
}
