
public class T1420do31 {
    public static void main(String[] args) {
        
        a: do try {
            throw new Exception();
        } catch (Exception e) {
            continue a;
        } finally {
            return;
        } while (false);
        {}
    
    }
}
