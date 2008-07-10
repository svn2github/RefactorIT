
public class T1420catch6 {
    public static void main(String[] args) {
        
        try {
            new Object();
        } catch (RuntimeException e) {
        } catch (Error err) {
        }
    
    }
}
