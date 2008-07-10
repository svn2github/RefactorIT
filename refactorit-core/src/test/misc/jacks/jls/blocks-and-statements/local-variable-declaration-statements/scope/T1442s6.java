
public class T1442s6 {
    public static void main(String[] args) {
        
        final Object i = null;
        new Object() {
            {
                Object o = i;
                for (int i = 1; i < 1; );
                o = i;
            }
        };
    
    }
}
