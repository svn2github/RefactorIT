
public class T1420try10 {
    public static void main(String[] args) {
        
        try {
            throw new Exception();
        } catch (Exception e) {
            return;
        } finally {
        }
        {}
    
    }
}
