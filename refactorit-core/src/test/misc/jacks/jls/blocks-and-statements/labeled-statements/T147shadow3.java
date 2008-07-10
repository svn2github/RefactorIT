
public class T147shadow3 {
    public static void main(String[] args) {
        
        test: new Object() {
            void foo() {
                int i;
                test: i = 1;
            }
        };
    
    }
}
