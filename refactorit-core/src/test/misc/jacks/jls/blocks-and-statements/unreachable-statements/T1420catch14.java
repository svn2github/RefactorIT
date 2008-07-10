
public class T1420catch14 {
    public static void main(String[] args) {
        
        Exception e = new Exception(); // create here to make test work
        try {
            try {
                throw e;
            } finally {
                return;
            }
        } catch (Exception e1) {
            // this is unreachable
        }
    
    }
}
