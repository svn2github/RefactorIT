
public class T15951e3 {
    public static void main(String[] args) {
        
        try {
            new Object() {
                int i = foo();
                int foo() throws InterruptedException { return 0; }
            };
        } catch (InterruptedException e) {
        }
    
    }
}
