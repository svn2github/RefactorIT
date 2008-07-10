
public class T1415try2 {
    public static void main(String[] args) {
        
        a: do {
            try {
                try {
                    continue a;
                } finally { // discard the continue
                    throw new Exception();
                }
            } catch (Throwable t) { // stop the exception
            }
            int i = 1; // reachable, even though it follows a continue
        } while (false);
    
    }
}
