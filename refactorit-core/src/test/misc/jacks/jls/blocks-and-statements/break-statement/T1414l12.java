
public class T1414l12 {
    public static void main(String[] args) {
        
        a: do {
            new Object() {
                {
                    break a;
                }
            };
        } while (false);
    
    }
}
