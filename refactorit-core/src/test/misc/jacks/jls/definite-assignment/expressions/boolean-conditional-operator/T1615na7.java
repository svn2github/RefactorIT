
public class T1615na7 {
    public static void main(String[] args) {
        
	try {
	    throw new Exception();
	} catch (final Exception x) {
	    if (true ? true : ((x = null) == null));
	}
    
    }
}
