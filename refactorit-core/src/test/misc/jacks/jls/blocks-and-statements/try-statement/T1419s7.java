
public class T1419s7 {
    public static void main(String[] args) {
        
        try {
            throw new ArithmeticException();
        } catch (final ArithmeticException i) {
            new Object() {
                {
                    try {
                        ArithmeticException ae = i;
                        throw new NullPointerException();
                    } catch (NullPointerException i) {
                    }
                }
            };
        }
    
    }
}
