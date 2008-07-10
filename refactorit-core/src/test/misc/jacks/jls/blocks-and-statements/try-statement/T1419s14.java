
public class T1419s14 {
    public static void main(String[] args) {
        
        try {
            throw new Exception();
        } catch (final Exception e) {
            new Object() {
                {
                    Object o = e;
                    for (int e = 1; e < 1; );
                    o = e;
                }
            };
        }
    
    }
}
