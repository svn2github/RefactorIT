
public class T1420label12 {
    public static void main(String[] args) {
        
        a: try {
            throw new Exception();
        } catch (Exception e) {
            break a;
        } finally {
            return;
        }
        int i;
    
    }
}
