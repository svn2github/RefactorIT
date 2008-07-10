
public class T1419s15 {
    public static void main(String[] args) {
        
        try {
            throw new ArithmeticException();
        } catch (final ArithmeticException e) {
            new Object() {
                {
                    try {
                        ArithmeticException a = e;
                        throw new NullPointerException();
                    } catch (NullPointerException e) {
                    }
                }
            };
        }
    
    }
}
