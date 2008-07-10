
public class T1420catch15 {
    public static void main(String[] args) {
        
        try {
            try {
                throw new java.io.IOException();
            } finally {
                return;
            }
        } catch (java.io.IOException e) {
            // this is unreachable
        }
    
    }
}
