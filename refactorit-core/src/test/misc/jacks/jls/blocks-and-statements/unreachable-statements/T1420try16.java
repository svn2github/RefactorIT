
public class T1420try16 {
    public static void main(String[] args) {
        
        try {
            new Object();
        } catch (Exception e) {
            return;
        } finally {
            return;
        }
        { int x=3; }
    
    }
}
