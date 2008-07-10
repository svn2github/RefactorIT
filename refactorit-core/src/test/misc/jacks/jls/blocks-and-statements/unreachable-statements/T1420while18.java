
public class T1420while18 {
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
        {}
    
    }
}
