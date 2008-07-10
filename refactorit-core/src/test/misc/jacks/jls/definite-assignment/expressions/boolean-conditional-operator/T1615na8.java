
public class T1615na8 {
    public static void main(String[] args) {
        
	try {
	    throw new Exception();
	} catch (final Exception x) {
	    if (false ? (x = null) == null : false);
	}
    
    }
}
