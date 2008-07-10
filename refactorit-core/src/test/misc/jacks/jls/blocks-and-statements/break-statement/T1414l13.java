
public class T1414l13 {
    public static void main(String[] args) {
        
        a: do {
            try {
                try {
                    break a;
                } finally { // discard the break
                    throw new Exception();
                }
            } catch (Throwable t) { // stop the exception
            }
            int i = 1; // reachable, even though it follows a break
        } while (false);
    
    }
}
