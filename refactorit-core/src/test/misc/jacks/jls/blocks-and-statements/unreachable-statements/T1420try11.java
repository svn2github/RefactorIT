
public class T1420try11 {
    public static void main(String[] args) {
        
        try {
            throw new Exception();
        } catch (Exception e) {
            return;
        } finally {
        }
        { int x=3; }
    
    }
}
