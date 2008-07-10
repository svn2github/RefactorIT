
public class T1420for19 {
    public static void main(String[] args) {
        
        for ( ; ; )
            try {
                throw new Exception();
            } catch (Exception e) {
                break;
            } finally {
                return;
            }
        int i;
    
    }
}
