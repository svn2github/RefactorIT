
public class T1414try1 {
    public static void main(String[] args) {
        
        do {
            try {
                try {
                    break;
                } finally { // discard the break
                    throw new Exception();
                }
            } catch (Throwable t) { // stop the exception
            }
            int i = 1; // reachable, even though it follows a break
        } while (false);
    
    }
}
