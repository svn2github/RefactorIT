
public class T1420for22 {
    public static void main(String[] args) {
        
        for ( ; ; )
            try {
                throw new Exception();
            } catch (Exception e) {
                break;
            } finally {
                return;
            }
        { int x=3; }
    
    }
}
