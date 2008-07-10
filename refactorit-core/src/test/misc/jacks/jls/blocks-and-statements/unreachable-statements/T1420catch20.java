
public class T1420catch20 {
    public static void main(String[] args) {
        
        class MyException extends ArithmeticException {}
        try {
            int i = 0;
            i /= i;
        } catch (MyException e) {
            // unreachable, as expressions do not throw subclass
        }
    
    }
}
