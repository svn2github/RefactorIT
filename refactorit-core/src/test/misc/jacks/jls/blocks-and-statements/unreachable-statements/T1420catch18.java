
public class T1420catch18 {
    public static void main(String[] args) {
        
        try {
            try {
                throw new Exception();
            } catch (Throwable t) {
            }
        } catch (Exception e) {
            // this is unreachable
        }
    
    }
}
