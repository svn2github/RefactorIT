
public class T1420while19 {
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
        { int x=3; }
    
    }
}
