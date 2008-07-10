
public class T1442s5 {
    public static void main(String[] args) {
        
        final Object i = null;
        new Object() {
            {
                Object o = i;
                int i;
                i = 1;
            }
        };
    
    }
}
