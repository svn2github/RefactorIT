
public class T1419s12 {
    public static void main(String[] args) {
        
        try {
            throw new Exception();
        } catch (final Exception e) {
            new Object() {
                Object o = e;
                void foo(int e) {}
            };
        }
    
    }
}
