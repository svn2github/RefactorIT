
public class T1420while17 {
    public static void main(String[] args) {
        
        while (true) {
            try {
                throw new Exception();
            } catch (Exception e) {
                break;
            } finally {
                return;
            }
        }
        ;
    
    }
}
