
public class T1420catch10 {
    public static void main(String[] args) {
        
        int i, j=0;
        try {
            i = 1/j;
        } catch (ArithmeticException ae) {
        } catch (RuntimeException re) {
            // the only possible exception, ae, has already been caught
        }
    
    }
}
