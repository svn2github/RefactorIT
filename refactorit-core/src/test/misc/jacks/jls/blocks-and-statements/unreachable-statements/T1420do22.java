
public class T1420do22 {
    public static void main(String[] args) {
        
        do try {
            throw new Exception();
        } catch (Exception e) {
            continue;
        } finally {
            return;
        } while (false);
        ;
    
    }
}
