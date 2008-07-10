
public class T1420catch17 {
    public static void main(String[] args) {
        
        try {
            try {
                throw new java.io.IOException();
            } catch (java.io.IOException e) {
                throw e;
            } finally {
                return;
            }
        } catch (java.io.IOException e1) {
            // this is unreachable
        }
    
    }
}
