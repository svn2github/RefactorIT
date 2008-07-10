
public class T1442s8 {
    public static void main(String[] args) {
        
        final Object i = null;
        new Object() {
            Object o = i;
            void foo(int i) {
                i = 1;
            }
        };
    
    }
}
