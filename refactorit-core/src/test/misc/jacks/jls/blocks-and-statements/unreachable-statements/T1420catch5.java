
public class T1420catch5 {
    public static void main(String[] args) {
        
        try {
            throw new Exception();
        } catch (Exception e) {
        } catch (Throwable t) {
            // possible Error creating new Exception
        }
    
    }
}
