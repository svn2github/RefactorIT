
public class T1420do13 {
    public static void main(String[] args) {
        
        do try {
            throw new Exception();
        } catch (Exception e) {
            break;
        } finally {
            return;
        } while (true);
        int i;
    
    }
}
