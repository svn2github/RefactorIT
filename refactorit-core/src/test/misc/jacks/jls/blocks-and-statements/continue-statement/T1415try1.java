
public class T1415try1 {
    public static void main(String[] args) {
        
        do {
            try {
                try {
                    continue;
                } finally { // discard the continue
                    throw new Exception();
                }
            } catch (Throwable t) { // stop the exception
            }
            int i = 1; // reachable, even though it follows a continue
        } while (false);
    
    }
}
