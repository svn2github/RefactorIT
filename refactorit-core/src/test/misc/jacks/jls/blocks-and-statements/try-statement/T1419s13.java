
public class T1419s13 {
    public static void main(String[] args) {
        
        try {
            throw new Exception();
        } catch (final Exception e) {
            new Object() {
                {
                    Object o = e;
                    int e;
                    e = 1;
                }
            };
        }
    
    }
}
