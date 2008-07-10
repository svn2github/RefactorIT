
public class T1420try20 {
    public static void main(String[] args) {
        
        try {
            throw new Exception();
        } catch (Exception e) {
            return;
        } finally {
            return;
        }
        { int x=3; }
    
    }
}
