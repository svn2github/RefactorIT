
public class T1615daf11 {
    public static void main(String[] args) {
        
	boolean a = false, x;
	if (a ? false : (x = false));
	else a = x;
    
    }
}
