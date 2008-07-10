
public class T1420try22 {
    public static void main(String[] args) {
        
        try {
            throw new Exception();
        } catch (Exception e) {
            return;
        } catch (Throwable t) {
        } finally {
            return;
        }
        ;
    
    }
}
